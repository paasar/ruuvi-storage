(ns ruuvi-storage.repository
  (:require [clojure.java.jdbc :as j]
            [clojure.tools.logging :refer [info]]))

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "./ruuvi-storage.db"})

(defn initiate-db! []
  (info "Initiating database")
  (j/execute! db "CREATE TABLE IF NOT EXISTS info (
                   version INTEGER DEFAULT 1)")
  (j/execute! db "CREATE TABLE IF NOT EXISTS measurements (
                   id INTEGER PRIMARY KEY,
                   name VARCHAR NOT NULL,
                   temperature INTEGER NOT NULL,
                   pressure INTEGER NOT NULL,
                   humidity INTEGER NOT NULL,
                   created TIMESTAMP DEFAULT CURRENT_TIMESTAMP)"))

(defn save! [{tag-name :name {:keys [temperature pressure humidity]} :data}]
  (j/insert! db :measurements {:name tag-name
                               :temperature temperature
                               :pressure pressure
                               :humidity humidity}))

(defn measurements [& {limit :limit :or {limit 30}}]
  (j/query db ["SELECT name,
                       temperature,
                       pressure,
                       humidity,
                       datetime(created, 'localtime') as created
                  FROM measurements
                  ORDER BY created DESC
                  LIMIT ?"
               limit]))
