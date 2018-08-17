(ns elo.date-picker-utils
  (:require [cljsjs.react-datepicker]
            [goog.date.Date]
            [reagent.core :as reagent]))

(defn date-time-picker
  "Self-contained date picker component.

  Takes keys:

  - :date - initial ISOdate string (required)
  - :react-key - unique react key (required)
  - :name - value for name attribute of datepicker input
  - :placeholder -value for placeholder attribute of datepicker input
  - :on-change - A function of arity 1 that receives date values on change.
  - :min-date - Earliest day to select
  - :max-date - Latest day to select
  - :class - A string of space-separated CSS classes for the containing div."
  [{:keys [date]}]
  (let [selected-date (reagent/atom (when date (js/moment date)))]
    (fn [{:keys [class
                name
                on-change
                placeholder
                react-key
                min-date
                max-date]}]
      [:div.date-range-input__side {:key react-key
                                    :class class}
       (js/React.createElement
        (.-default js/DatePicker)
        (cond-> {:selected @selected-date
                 :isClearable false
                 ;; :showTimeSelect true
                 :timeFormat "HH:mm"
                 :timeIntervals 15
                 :dateFormat "LLL"
                 :timeCaption "time"
                 :minDate (js/moment min-date)
                 :maxDate (js/moment max-date)
                 :onChange #(do
                              (reset! selected-date %)
                              (when on-change (on-change %)))}

          ;;#(within-filter-panels? %) identity
          placeholder (assoc :placeholderText placeholder)
          name (assoc :name name)
          true clj->js))])))
