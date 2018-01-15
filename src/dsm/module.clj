(ns dsm.module
  (:require
    [duct.core :as core]
    [duct.core.env :as env]
    [duct.core.merge :as merge]
    [integrant.core :as ig]))

(defn- get-environment [config options]
  (:environment options (:duct.core/environment config :production)))

(def base-config
  {:dsm/mailer
   ^:demote {:type           "test-mailer"
             :logger         nil
             :templates-path nil
             :save-path      nil
             ;; smtp stuff
             :host           nil
             :user           nil
             :pass           nil
             :ssl            true
             :port           nil
             :tls            nil
             :from           nil
             :reply-to       nil}})

(def ^:private env-configs
  {:production (assoc-in base-config [:dsm/mailer :type] "smtp-mailer")
   :development base-config})

(defmethod ig/init-key :dsm/module [_ options]
  {:req #{:duct/logger}
   :fn (fn [config]
         (->> (get-environment config options)
              env-configs
              (core/merge-configs config)))})
