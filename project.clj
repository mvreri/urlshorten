(defproject urlshorten "0.1.0-SNAPSHOT"

  :description "@mvreri"
  :url "https://someurlshorten.url"

  :dependencies [[prone "0.8.0"]
                 [log4j
                  "1.2.17"
                  :exclusions
                  [javax.mail/mail
                   javax.jms/jms
                   com.sun.jdmk/jmxtools
                   com.sun.jmx/jmxri]]
                 [selmer "0.7.7"]
                 [markdown-clj "0.9.58" :exclusions [com.keminglabs/cljx]]
                 [im.chit/cronj "1.4.3"]
                 [com.taoensso/timbre "3.3.1"]
                 [com.h2database/h2 "1.4.182"]
                 [noir-exception "0.2.5"]
                 [korma "0.4.3"]
                 [lib-noir "0.9.9"]
                 [org.clojure/clojure "1.6.0"]
                 [environ "1.1.0"]
                 [clj-pdf "2.2.31"]
                 [clj-time "0.14.3"]
                 [clj-http "3.8.0"]
                 [com.taoensso/tower "3.0.2"]
                 [org.postgresql/postgresql "42.2.2"]
                 [ragtime "0.3.6"]
                 [mysql/mysql-connector-java "5.1.34"]
                 [org.clojure/java.jdbc "0.3.5"]
                 [org.clojure/data.json "0.2.5"]
                 [clojure.joda-time "0.2.0"]
                 [lein-ring "0.12.4"]
                 [hiccup "1.0.5"]
                 [ring/ring-anti-forgery "1.3.0"]
                 [cheshire "5.9.0"]
                 [twitter-api "1.8.0"]
                 [byte-streams "0.2.4"]
                 [image-resizer "0.1.10"]
                 [org.apache.pdfbox/pdfbox "1.8.2"]
                 [http.async.client "0.5.2"]
                 [crypto-password "0.2.1"]
                 [metosin/reitit "0.5.15"]                  ;trimming trailing slashes
                 [metosin/spec-tools "0.10.5"]              ;making sure the ring routes are trimmed
                 [slingshot "0.12.2"]                       ;for try catch in cljhttp
                 [metosin/ring-http-response "0.9.3"]
                 [com.fzakaria/slf4j-timbre "0.3.21"]       ;added to rtemove this error in the log MLog initialization issue: slf4j found no binding or threatened to use its (dangerously silent) NOPLogger. We consider the slf4j library not found.
                 [org.clojure/math.numeric-tower "0.0.5"]
                 [ring/ring-core "1.9.4"]                   ;for the html escapes
                 ]

  :min-lein-version "2.0.0"
  :uberjar-name "urlshorten.jar"
  :repl-options {:init-ns urlshorten.handler}
  :jvm-opts ["-server"]
  :main urlshorten.core
  :plugins [[lein-ring "0.12.5"]
            [lein-environ "1.1.0"]
            [lein-ancient "0.6.5"]
            [ragtime/ragtime.lein "0.3.8"]]

  :ring {:handler urlshorten.handler/app
         :init    urlshorten.handler/init
         :destroy urlshorten.handler/destroy
         :uberwar-name "urlshorten.war"}
  
  :ragtime
  {:migrations ragtime.sql.files/migrations
   :database "jdbc:h2:./site.db"}

  :profiles
  {:uberjar {:omit-source true
             :env {:production true}
             :aot :all}
   :dev {:dependencies [[ring-mock "0.1.5"]
                        [ring/ring-devel "1.3.2"]
                        [pjstadig/humane-test-output "0.7.0"]
                        ]
         :source-paths ["env/dev/clj"]
         :repl-options {:init-ns urlshorten.repl}
         :injections [(require 'pjstadig.humane-test-output)
                      (pjstadig.humane-test-output/activate!)]
         :env {:dev true}}})
