(ns ruuvi-storage.alarm
  (:require [clojure.tools.logging :refer [info]]
            [java-time :as t]
            [ruuvi-storage.email :refer [send-mail]]
            [ruuvi-storage.repository :as repo]))

(def ^:private addresses (atom {}))

(def ^:private alarm (atom false))

(defn alarm-set? []
  @alarm)

(defn reset-alarm! []
  (reset! alarm false))

(defn set-addresses [from to]
  (info (format "Setting alarm email from='%s' and to='%s'." from to))
  (reset! addresses {:from from :to to}))

(defn- temperatures []
  (->> (repo/measurements 100)
       vals
       flatten
       (mapv :temperature)))

(defn- measurements-within-thresholds? []
  (let [temps (temperatures)
        minimum (apply min temps)
        maximum (apply max temps)]
    (<= 15 minimum maximum 24)))

(defn- send-alarm-mail []
  (info "Sending alarm mail")
  (let [{:keys [from to]} @addresses
        date-time (t/format "yyyy-MM-dd HH:mm" (t/local-date-time))]
    (send-mail from
               to
               (format "Temperature check alarm - %s" date-time)
               "Temperatures outside defined thresholds. Please check the situation.")))

(defn check-temperatures! []
  (when-not @alarm
    (when-not (measurements-within-thresholds?)
      (send-alarm-mail)
      (reset! alarm true))))

