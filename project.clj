(defproject imagez "0.5.0"
  :description "Image processing library for Clojure"
  :dependencies [[org.clojure/clojure "1.7.0-beta2"]
                 [net.mikera/cljunit "0.3.1"]
                 [net.mikera/clojure-utils "0.6.1"]
                 [org.imgscalr/imgscalr-lib "4.2"]
                 [net.mikera/mathz "0.3.0"]
                 [net.mikera/mikera-gui "0.3.1"]
                 [net.mikera/randomz "0.3.0"]
                 [com.jhlabs/filters "2.0.235-1"]]
  :repositories {"clojars.org" "http://clojars.org/repo"}
  :parent [net.mikera/clojure-pom "0.4.0"]
  :source-paths ["src/main/clojure"]
  :test-paths ["src/test/clojure"]
  :resource-paths ["src/test/resources"])
