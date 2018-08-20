(ns elo.dropdown
  (:require [cljsjs.selectize]
            [reagent.core :as reagent]
            [re-frame.core :as rf]))

;; $('#select-beast').selectize({
;;     create: true,
;;     sortField: 'text'
;; });

(def sample-options (atom ["one"  "two" "three"]))

(defn drop-inner
  []
  (let [update (fn [comp]
                 (.selectize ))]

    (reagent/create-class
     {:reagent-render (fn []
                        [:div])

      :component-did-mount (fn [comp]
                             (update comp))

      :component-did-update update
      :display-name "dropdown-inner"})))

(defn drop-outer
  []
  (let [pos (rf/subscribe [:possibilities])]
    [drop-inner @pos]))


;;TODO: google maps implementation
(defn gmap-inner []
  (let [gmap    (atom nil)
        options (clj->js {"zoom" 9})
        update  (fn [comp]
                  (let [{:keys [latitude longitude]} (reagent/props comp)
                        latlng (js/google.maps.LatLng. latitude longitude)]
                    (.setPosition (:marker @gmap) latlng)
                    (.panTo (:map @gmap) latlng)))]

    (reagent/create-class
     {:reagent-render (fn []
                        [:div
                         [:h4 "Map"]
                         [:div#map-canvas {:style {:height "400px"}}]])

      :component-did-mount (fn [comp]
                             (let [canvas  (.getElementById js/document "map-canvas")
                                   gm      (js/google.maps.Map. canvas options)
                                   marker  (js/google.maps.Marker. (clj->js {:map gm :title "Drone"}))]
                               (reset! gmap {:map gm :marker marker}))
                             (update comp))

      :component-did-update update
      :display-name "gmap-inner"})))

(def sample-position {:latitude 42 :longitude 0})

(defn gmap-outer []
  (let [pos (atom sample-position)]   ;; obtain the data

    (fn []
      [gmap-inner @pos])))
