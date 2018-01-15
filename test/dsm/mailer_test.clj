(ns dsm.mailer-test
  (:require
    [clojure.test :refer :all]
    [duct.core      :as duct]
    [integrant.core :as ig]
    [dsm.main]
    [dsm.mailer     :as mailer]))

(duct/load-hierarchy)

(derive :duct.logger/fake :duct/logger)

(deftest key-test
  (is (isa? :dsm/module :duct/module))
  (is (isa? :dsm/mailer :duct/mailer)))

(def base-mailer-system
  {:dsm/module {},
   :dsm/mailer {:ssl       true,
                :save-path nil,
                :tls       nil,
                :port      nil,
                :host      nil,
                :from      nil,
                :logger    nil,
                :reply-to  nil,
                :pass      nil,
                :user      nil,
                :templates-path nil}})


(deftest module-test
  (testing "blank production config"
    (is (= (-> {:duct.core/environment :production,
                :duct.logger/fake      {}}
               (merge base-mailer-system)
               (assoc-in [:dsm/mailer :deliveries] nil)
               (assoc-in [:dsm/mailer :type] "smtp-mailer"))

           (-> (duct/prep {:duct.core/environment :production
                           :duct.logger/fake      {}
                           :dsm/module            {}})
               (assoc-in [:dsm/mailer :deliveries] nil)))))

  (testing "blank development config"
    (is (= (-> {:duct.core/environment :development,
                :duct.logger/fake {}}
               (merge base-mailer-system)
               (assoc-in [:dsm/mailer :deliveries] nil)
               (assoc-in [:dsm/mailer :type] "test-mailer"))

           (-> (duct/prep {:duct.core/environment :development
                           :duct.logger/fake      {}
                           :dsm/module            {}})
               (assoc-in [:dsm/mailer :deliveries] nil))))))

(defmethod mailer/email-model :registration
  [template-type lang to data]
  (merge data {:subject "Simple registration"
               :team    "Kind regards,\nThe Team"}))

(defmethod mailer/email-template :registration
  [template-type lang templates-path template-content-type]
  "Hi, %s %s. You are registered. %s")

(defmethod mailer/email-render-template :default
  [template-type lang model template]
  (->> [model]
       (apply (juxt :first-name :last-name :team))
       (apply (partial format template))))

(deftest test-mailer-test
  (testing "should deliver email"
    (let [mailer (ig/init-key :dsm/mailer {:type "test-mailer"
                                           :from "MJ <some-email@example.com>"})
          deliveries (-> mailer
                         (mailer/deliver! :registration
                                          "some-email@example.com"
                                          :en ;or nil
                                          true
                                          true
                                          {:first-name "Jan", :last-name "Kowalski"})
                         :deliveries
                         deref)]
      (is (-> deliveries count (= 1)))
      (is (-> deliveries first :body first (= :alternative)))
      (doseq [content [(-> deliveries first :body (nth 1) :content)
                       (-> deliveries first :body (nth 2) :content)]]
        (is (= "Hi, Jan Kowalski. You are registered. Kind regards,\nThe Team" content)))
      (ig/halt-key! :dsm/mailer mailer))))


#_(comment
  ; Testing from the REPL
  (let [email "some-email@example.com"]
    (-> (ig/init-key :dsm/mailer {:type "smtp-mailer"
                                  :from (format "MJ <%s>" email)
                                  :host "smtp.gmail.com"
                                  :user email
                                  :pass "123456789"})
        (mailer/deliver! :registration
                         :en ;or nil
                         email
                         true
                         true
                         {:first-name "Mariusz", :last-name "Jachimowicz"})
        println)))
