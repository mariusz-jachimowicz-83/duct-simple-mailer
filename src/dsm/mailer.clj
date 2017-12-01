(ns dsm.mailer
  (:require
    [duct.logger    :as logger]
    [integrant.core :as ig]
    [postal.core    :as postal]))

(defn- cant-deliver
  [template-type kw]
  (format "Can't deliver %s email. %s value is required." template-type kw))

(defn- email-envelope
  [mailer to data body]
  (->> {:to (or to (:to data)), :subject (:subject data), :body body}
       (merge (select-keys mailer [:from :reply-to])
              (select-keys data   [:from :reply-to]))))

(defn email-body [html text]
  (let [txt-body  (when text {:content text, :type "text/plain; charset=utf-8"})
        html-body (when html {:content html, :type "text/html;  charset=utf-8"})
        body      (remove nil? [:alternative txt-body html-body])]
    (if (= (count body) 2) [(second body)] (vec body))))

(defmulti email-model
  "Returns data required for email render phase"
  (fn [template-type lang to data]
    template-type))

(defmulti email-template
  "Returns email template required for email render phase"
  (fn [template-type lang templates-path template-content-type]
    template-type))

(defmulti email-render-template
  "Render email based on data and template"
  (fn [template-type lang model template]
    template-type))

(defmulti deliver-email!
  "Send, store or write email somewhere"
  (fn [mailer template-type lang to data body]
    (assert (or to (:to data)) (cant-deliver template-type :to))
    (assert (or (:from mailer)
                (:from data))  (cant-deliver template-type :from))
    (-> mailer :type keyword)))

(defmethod deliver-email! :test-mailer
  [{:keys [deliveries] :as mailer} template-type lang to data body]
  (swap! (:deliveries mailer) conj (email-envelope mailer lang data body))
  mailer)

(defmethod deliver-email! :smtp-mailer
  [mailer template-type lang to data body]
  (->> (email-envelope mailer lang data body)
       (postal/send-message (dissoc mailer :from :reply-to))))

(defmethod deliver-email! :log-mailer
  [{:keys [logger] :as mailer} template-type lang to data]
  (assert logger "Logger is required")
  (println "TODO: implement log-mailer"))

(defmethod deliver-email! :file-mailer
  [{:keys [save-path] :as mailer} template-type lang to data]
  (println "TODO: implement file-mailer"))

(def base-config
  {:type           "test-mailer"
   :logger         nil
   :templates-path nil
   :save-path      nil
   :deliveries     (atom [])
   ;; smtp stuff
   :host           nil
   :user           nil
   :pass           nil
   :ssl            true
   :port           nil
   :tls            nil
   :from           nil
   :reply-to       nil})

(defprotocol IMailer
  (deliver! [mailer template-type lang to html? text? data]))

(defrecord Mailer []
  IMailer
  (deliver! [mailer template-type lang to html? text? data]
    (let [model (email-model template-type lang to data)
          load-template (partial email-template
                                 template-type
                                 lang
                                 (:templates-path mailer))
          html (when html?
                 (->> (load-template :html)
                      (email-render-template template-type lang model)))
          text (when text?
                 (->> (load-template :text)
                      (email-render-template template-type lang model)))
          body (email-body html text)]
    (deliver-email! mailer template-type lang to model body))))

(defmethod ig/init-key :dsm/mailer [_ config]
  (map->Mailer (merge base-config config)))

(defmethod ig/halt-key! :dsm/mailer [_ {:keys [deliveries] :as config}]
  (reset! deliveries []))
