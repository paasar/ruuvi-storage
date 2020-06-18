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
    (<= 15 minimum maximum 28)))

(defn now []
  (t/local-date-time))

(defn- measurements-stale? []
  (when-let [created (-> (repo/measurements 1) vals ffirst :created)]
    (let [created-date-time (t/local-date-time "yyyy-MM-dd HH:mm:ss" created)]
      (neg? (.compareTo (t/plus created-date-time (t/hours 2)) (now))))))

(defn- send-alarm-mail [reason]
  (info "Sending alarm mail")
  (let [{:keys [from to]} @addresses
        date-time (t/format "yyyy-MM-dd HH:mm" (t/local-date-time))]
    (send-mail from
               to
               (format "Temperature check alarm - %s" date-time)
               (str reason " Please check the situation."))))

(defn check-temperatures! []
  (when-not @alarm
    (when-not (measurements-within-thresholds?)
      (send-alarm-mail "Temperatures outside defined thresholds.")
      (reset! alarm true))
    (when (measurements-stale?)
      (send-alarm-mail "Measurements are over 2 hours old.")
      (reset! alarm true))))

