(defproject ruuvi-storage "0.1.0"
  :description "Server for saving and rendering RuuviTag data"
  :url "http://github.com/paasar/ruuvi-storage"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [camel-snake-kebab "0.4.0"]
                 [cheshire "5.9.0"]
                 [compojure "1.6.1"]
                 [hiccup "1.0.5"]
                 [org.clojure/java.jdbc "0.7.10"]
                 [org.clojure/tools.logging "0.5.0"]
                 [org.xerial/sqlite-jdbc "3.28.0"]
                 [ring/ring-defaults "0.3.2"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler ruuvi-storage.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}})
