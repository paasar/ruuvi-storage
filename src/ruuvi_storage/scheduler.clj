(ns ruuvi-storage.scheduler
  (:require [clojure.tools.logging :refer [error info]]
            [ruuvi-storage.alarm :refer [check-temperatures!]])
  (:import [java.util.concurrent ScheduledThreadPoolExecutor TimeUnit]))

(defn- stop-executor [^ScheduledThreadPoolExecutor executor]
  (.shutdown executor)
  (.awaitTermination executor 5 TimeUnit/SECONDS)
  (.shutdownNow executor))

(defn- with-exception-handling [f]
  (try
    (f)
    (catch Exception e
      (error e "Failed to execute background job."))
    (catch Throwable e ;For non-recoverable errors
      (error e "Failed to execute background job.")
      (throw e))))

(defn start-background-jobs! []
  (let [executor (ScheduledThreadPoolExecutor. 1)
        initial-delay 10
        one-hour-in-seconds 3600
        delay-between (* 1 one-hour-in-seconds)
        check-temperatures-job #(with-exception-handling check-temperatures!)]
    (info "Starting background jobs.")
    (.scheduleAtFixedRate executor
                          check-temperatures-job
                          initial-delay
                          delay-between
                          TimeUnit/SECONDS)
    #(do
       (info "Stopping background jobs.")
       (stop-executor executor))))
