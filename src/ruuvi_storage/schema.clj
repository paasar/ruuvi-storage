(ns ruuvi-storage.schema
  (:require [clojure.spec.alpha :as s]))

(s/def ::name string?)
(s/def ::temperature (s/and number? #(> % -50) #(< % 100)))
(s/def ::pressure (s/and number? #(> % 500) #(< % 1500)))
(s/def ::humidity (s/and number? pos? #(< % 101)))

(s/def ::data (s/keys :req-un [::temperature ::pressure ::humidity]))

(s/def ::measurement (s/keys :req-un [::name ::data]))

(s/def ::measurements (s/coll-of ::measurement :min-count 1))

(defn valid-measurements? [measurements]
  (s/valid? ::measurements measurements))

(defn explain [measurements]
  (s/explain ::measurements measurements))
