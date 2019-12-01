(ns ruuvi-storage.alarm-test
  (:require [clojure.test :refer :all]
            [postal.core :refer [send-message]]
            [ruuvi-storage.alarm :refer [alarm-set? check-temperatures! reset-alarm!]]
            [ruuvi-storage.repository :refer [save!]]
            [ruuvi-storage.test-helpers :refer [with-mock-email with-test-db]]))

(defn- with-alarm-off [test]
  (reset-alarm!)
  (test))

(use-fixtures :each with-test-db with-alarm-off with-mock-email)

(deftest with-temperatures-within-limits
  (save! {:name "test-all-ok" :data {:temperature 16 :pressure 1000 :humidity 50}})
  (save! {:name "test-all-ok-2" :data {:temperature 23 :pressure 1000 :humidity 50}})
  (let [send-message-called (atom false)]
    (with-redefs [send-message (fn [& _] (reset! send-message-called true))]
      (testing "should not set alarm and not send mail"
        (check-temperatures!)
        (is (false? (alarm-set?)))
        (is (false? @send-message-called))))))

(deftest with-temperature-lower-than-threshold
  (save! {:name "test-temp-too-low" :data {:temperature 14.9 :pressure 1000 :humidity 50}})
  (let [send-message-called (atom false)]
    (with-redefs [send-message (fn [& _] (reset! send-message-called true))]
      (testing "should set alarm and send mail"
        (check-temperatures!)
        (is (true? (alarm-set?)))
        (is (true? @send-message-called))))))

(deftest with-temperature-lower-than-threshold-and-alarm-on
  (save! {:name "test-temp-too-low" :data {:temperature 14.9 :pressure 1000 :humidity 50}})
  (check-temperatures!) ;set the alarm on
  (let [send-message-called (atom false)]
    (with-redefs [send-message (fn [& _] (reset! send-message-called true))]
      (testing "should not send mail"
        (check-temperatures!)
        (is (true? (alarm-set?)))
        (is (false? @send-message-called))))))

(deftest with-temperature-higher-than-threshold
  (save! {:name "test-temp-too-high" :data {:temperature 24.1 :pressure 1000 :humidity 50}})
  (let [send-message-called (atom false)]
    (with-redefs [send-message (fn [& _] (reset! send-message-called true))]
      (testing "should set alarm and send mail"
        (check-temperatures!)
        (is (true? (alarm-set?)))
        (is (true? @send-message-called))))))