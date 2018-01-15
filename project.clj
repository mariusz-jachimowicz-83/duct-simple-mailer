(defproject com.mjachimowicz/duct-simple-mailer "0.1.2-SNAPSHOT"
  :description "Very simple email sender component for Duct framework"
  :url "https://github.com/mariusz-jachimowicz-83/duct-simple-mailer"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-beta4"]
                 [duct/core "0.6.1"]
                 [duct/logger "0.2.1"]
                 [integrant "0.6.1"]
                 [com.draines/postal "2.0.2"]]
  :deploy-repositories [["clojars" {:sign-releases false}]]

  ;; lein cloverage --fail-threshold 95
  ;; lein kibit
  ;; lein eastwood
  :profiles {:dev {:plugins [[lein-cloverage "1.0.10"]
                             [lein-kibit "0.1.6"]
                             [jonase/eastwood "0.2.5"]]}})
