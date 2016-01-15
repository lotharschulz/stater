(ns neo.www
  (:require [neo.options.orders :refer [add-order find-orders]]
            [neo.db :refer [conn create-schema]]
            [neo.conf :refer [config]]
            [neo.options.engine :refer [match-quote]]
            [mount.core :refer [defstate]]
            [cheshire.core :refer [generate-string]]
            [compojure.core :refer [routes defroutes GET POST]]
            [compojure.handler :as handler]
            [ring.adapter.jetty :refer [run-jetty]]))

(defn- to-order [ticker qty bid offer]
  ;; yes, validation :)
  {:ticker ticker 
   :bid (bigdec bid) 
   :offer (bigdec offer) 
   :qty (Integer/parseInt qty)})

(defroutes neo-routes

  (GET "/" [] "welcome to neo options exchange!")

  (GET "/neo/orders/:ticker" [ticker]
       (generate-string (find-orders conn ticker)))

  (GET "/neo/match-quote" [ticker qty bid offer]
       (let [book (find-orders conn ticker)
             quote (to-order ticker qty bid offer)]
         (generate-string {:matched (match-quote quote book)})))

  (POST "/neo/orders" [ticker qty bid offer] 
        (let [order (to-order ticker qty bid offer)]
          (add-order conn order)
          (generate-string {:added order}))))

(defn start-neo [conn {:keys [www]}]     ;; app entry point
  (create-schema conn)                   ;; just an example, usually schema would already be there
  (-> (routes neo-routes)
      (handler/site)
      (run-jetty {:join? false
                  :port (:port www)})))

(defstate neo-app :start (start-neo conn config)
                  :stop (.stop neo-app))  ;; it's a "org.eclipse.jetty.server.Server" at this point
