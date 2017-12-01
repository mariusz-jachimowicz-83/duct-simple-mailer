(ns dsm.mailer-test
  (:require
    [clojure.test :refer :all]
    [duct.core      :as duct]
    [integrant.core :as ig]
    [dsm.mailer     :as mailer]))

(duct/load-hierarchy)

(deftest key-test
  (is (isa? :dsm/mailer :duct/module)))

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
    (let [mailer (ig/init-key :dsm/mailer {:from "MJ <some-email@example.com>"})
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

(comment
  ; Testing from the REPL
  (let [email "some-email@example.com"]
    (-> (ig/init-key :dsm/mailer {:from (format "MJ <%s>" email)
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
