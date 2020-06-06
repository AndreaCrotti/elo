(ns byf.firebase
  (:require [com.degel.re-frame-firebase :as firebase]
            [byf.config :as config]
            [re-frame.core :as rf]))

(rf/reg-event-fx
 :firebase-error
 (fn [& args]
   (println "Got args = " args)))

(rf/reg-event-fx
 :sign-in
 (fn [_ _] {:firebase/google-sign-in {:sign-in-method :popup}}))

(rf/reg-event-fx
 :sign-out
 (fn [_ _] {:firebase/sign-out nil}))

(rf/reg-event-db
 :set-user
 (fn [db [_ user]]
   (assoc db :user user)))

(rf/reg-sub
 :user
 :user)

;;; From https://console.firebase.google.com/u/0/project/trilystro/overview - "Add Firebase to your web app"
(defn firebase-app-info
  []
  {:apiKey     (config/value :firebase-api-key)
   :authDomain (config/value :firebase-auth-domain)
   :projectId "beatyourfriends-auth"})

;; TODO: add missing project-id
(defn ^:export init []
  (firebase/init :firebase-app-info      (firebase-app-info)
                 ; See: https://firebase.google.com/docs/reference/js/firebase.firestore.Settings
                 :firestore-settings     {:timestampsInSnapshots true}
                 :get-user-sub           [:user]
                 :set-user-event         [:set-user]
                 :default-error-handler  [:firebase-error]))
