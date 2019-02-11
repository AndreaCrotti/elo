(ns byf.auth
  (:require [byf.config :refer [value]]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.httpbasic :refer [http-basic-backend]]))

(defn admin-password
  []
  (value :admin-password))

(defn authdata
  "All possible authenticated users"
  []
  {:admin (admin-password)})

(defn authenticate
  [_ {:keys [username password]}]
  (or
   (nil? (admin-password))
   (when-let [user-password (get (authdata) (keyword username))]
     (when (= password user-password)
       (keyword username)))))

(def basic-auth-backend
  ;; change the realm depending on the environment
  (http-basic-backend {:realm "fifa-byf.herokuapp.com"
                       :authfn authenticate}))

(defmacro with-basic-auth
  [request body]
  `(if (authenticated? ~request)
     ~body
     (throw-unauthorized)))

(def oauth2-config
  {:github
   {:authorize-uri "https://github.com/login/oauth/authorize"
    :access-token-uri "https://github.com/login/oauth/access_token"
    :client-id (value :github-client-id)
    :client-secret (value :github-client-secret)
    :scopes ["user:email"]
    :launch-uri "/oauth2/github"
    :redirect-uri "/api/oauth2/github/callback"
    :landing-uri "/"
    :basic-auth? true}})
