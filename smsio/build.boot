(set-env!
  :source-paths #{"src"}
  :dependencies '[[mount                          "0.1.9-SNAPSHOT"]
                  [twilio-api                     "1.0.1"]
                  [compojure                      "1.4.0"]
                  [ring/ring-jetty-adapter        "1.1.0"]
                  [cheshire                       "5.5.0"]
                  [ch.qos.logback/logback-classic "1.1.3"]
                  [org.clojure/tools.logging      "0.3.1"]
                  [robert/hooke                   "1.3.0"]

                  [clj-http                       "2.0.0"     :scope "test"]
                  [org.clojure/core.async         "0.2.374"   :scope "test"]
                  [org.clojure/tools.namespace    "0.2.11"    :scope "provided"]
                  [org.clojure/tools.nrepl        "0.2.12"    :scope "provided"]

                  ;; boot clj
                  [boot/core                "2.5.1"           :scope "provided"]
                  [adzerk/bootlaces         "0.1.13"          :scope "test"]
                  [adzerk/boot-logservice   "1.0.1"           :scope "test"]
                  [adzerk/boot-test         "1.0.6"           :scope "test"]
                  [tolitius/boot-stripper   "0.1.0-SNAPSHOT"  :scope "test"]
                  [tolitius/boot-check      "0.1.1"           :scope "test"]])


(require '[tolitius.boot-check :as check]
         '[tolitius.boot-stripper :refer [strip-deps-attr]]
         '[clojure.tools.namespace.repl :refer [set-refresh-dirs]]
         '[adzerk.boot-test :as bt]
         '[adzerk.boot-logservice :as log-service]
         '[clojure.tools.logging :as log])

(def log4b
  [:configuration
   [:appender {:name "STDOUT" :class "ch.qos.logback.core.ConsoleAppender"}
    [:encoder [:pattern "%-5level %logger{36} - %msg%n"]]]
   [:root {:level "TRACE"}
    [:appender-ref {:ref "STDOUT"}]]])

(deftask dev []
  (set-env! :source-paths #(conj % "dev"))

  (alter-var-root #'log/*logger-factory*
                  (constantly (log-service/make-factory log4b)))

  (apply set-refresh-dirs (get-env :directories))

  (require 'dev)
  (in-ns 'dev))

(deftask test []
  (set-env! :source-paths #(conj % "test/app"))
  (bt/test))

(deftask check-sources []
  (comp
    (check/with-bikeshed)
    (check/with-eastwood)
    (check/with-yagni)
    (check/with-kibit)))
