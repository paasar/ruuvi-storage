(ns ruuvi-storage.email
  (:require [postal.core :refer [send-message]]))

(defn send-mail [from to subject message]
  (send-message {:host "localhost"}
                {:from from
                 :to to
                 :subject subject
                 :body message}))
