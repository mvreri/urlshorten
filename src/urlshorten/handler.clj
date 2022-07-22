(ns urlshorten.handler
  (:require [compojure.core :refer [defroutes]]
            [urlshorten.routes.home :refer [home-routes open-routes]]
            [urlshorten.middleware :refer [load-middleware]]
            [urlshorten.session-manager :as session-manager]
            [noir.session :as session]
            [noir.response :refer [redirect]]
            [noir.util.middleware :refer [app-handler]]
            [ring.middleware.defaults :refer [site-defaults]]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.rotor :as rotor]
            [selmer.parser :as parser]
            [environ.core :refer [env]]
            [cronj.core :as cronj]
            [urlshorten.config :as config]
            [urlshorten.layout :as layout]))

(defroutes base-routes
           (route/resources "/")
           (route/not-found (layout/render "site/404.html" {
                                                                 :product-name (str "The urlshorten")
                                                                 :product-description (str "The urlshorten")
                                                                 :page-title (str "You Seem Lost.")
                                                                 :today (clj-time.format/unparse (clj-time.format/formatter "yyyy-MM-dd") (clj-time.local/local-now))
                                                                 :year (clj-time.format/unparse (clj-time.format/formatter "yyyy") (clj-time.local/local-now))
                                                                 }))
           )

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (timbre/set-config!
    [:appenders :rotor]
    {:min-level             :info
     :enabled?              true
     :async?                false                           ; should be always false for rotor
     :max-message-per-msecs nil
     :fn                    rotor/appender-fn})

  (timbre/set-config!
    [:shared-appender-config :rotor]
    {:path "urlshorten.log" :max-size (* 512 1024) :backlog 10})

  (if (env :dev) (parser/cache-off!))
  ;;start the expired session cleanup job
  (cronj/start! session-manager/cleanup-job)
  (timbre/info "\n-=[ urlshorten started successfully"
               (when (env :dev) "using the development profile") "]=-"))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "urlshorten is shutting down...")
  (cronj/shutdown! session-manager/cleanup-job)
  (timbre/info "shutdown complete!"))

;; timeout sessions after 120 minutes
(def session-defaults
  {:timeout          (* 60 30)
   :timeout-response (redirect "/login")})

(defn- mk-defaults
  "set to true to enable XSS protection"
  [xss-protection?]
  (-> site-defaults
      (update-in [:session] merge session-defaults)
      (assoc-in [:security :anti-forgery] xss-protection?)))


(defn user-access[request]
  (session/get :sssn_in)                                   ;userrefno/sessionid
  ;(session/get :sssn_tp)                                   ;utype
  )

(defn wrap-body-string [handler]
  (fn [request]
    (let [body-str (ring.util.request/body-string request)]
      (handler (assoc request :body (java.io.StringReader. body-str))))))

(def app (->
           (app-handler
             ;; add your application routes here
             [home-routes open-routes base-routes]
             ;; add custom middleware here
             :middleware (load-middleware)
             :ring-defaults (mk-defaults false)
             ;; add access rules here
             :access-rules [{:uri "/*" :rule user-access :redirect "/login"}]
             ;; serialize/deserialize the following data formats
             ;; available formats:
             ;; :json :json-kw :yaml :yaml-kw :edn :yaml-in-html
             :formats [:json-kw :edn :transit-json])
           ;(wrap-body-string)
           ))