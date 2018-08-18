(ns elo.auth
  (:require [environ.core :refer [env]]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.auth.backends.httpbasic :refer [http-basic-backend]]))

(def ^:private local-pwd "secret")

(defn admin-password
  []
  (:admin-password env))

(defn authdata
  []
  "All possible authenticated users"
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
  (http-basic-backend {:realm "fifa-elo.herokuapp.com"
                       :authfn authenticate}))

(defmacro with-basic-auth
  [request body]
  `(if (authenticated? ~request)
     ~body
     (throw-unauthorized)))
