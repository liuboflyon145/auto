(ns leiningen.auto
  (:import [java.io File FilenameFilter])
  (:require [me.raynes.fs :as fs]
            [leiningen.core.main :refer :all]
            [cljfmt.core :refer :all]))

(defn auto
  "I don't do a lot."
  [project & args]
  (println "Hi!"))

(def ^:private sql-path "src/db/sqls/")
(def ^:private test-path "test/")
(def ^:private base "src/")

(defn- time-name []
  (-> "yyyyMMddHHmmss"
      java.text.SimpleDateFormat.
      (.format (java.util.Date.))))


(defn- ns-to-path [ns-name]
  (str (.. (str ns-name)
           (clojure.string/replace \. \/)
           (clojure.sting/replace \- \_))
       ".clj"))

(defn- gen-sql []
  (-> sql-path
      (str (time-name))
      (str ".sql")))

(defn- check-dir? [path]
  (if-not (fs/directory? path)
    (.mkdirs (fs/file path))))

(defn gen-mig
  {:doc "create default migration according to command
  to initializes the migration content"}
  [project & [command]]
  (try
    (let [path (gen-sql)
          init-content (case command
                         :create "#auto generate sql migration \ndrop table if exists table_name;\n
                                  create table if not exists table_name ();"
                         :insert "#auto generate sql migration \ninsert into table_name() values();"
                         :update "#auto generate sql migration \nupdate table table_name set ... where ...;"
                         :alter "#auto generate sql migration \nalter table table_name modify ....;"
                         :delete "#auto generate sql migration \ndelete from table table_name where ...;"
                         :drop "#auto generate sql migration \ndrop table table_name;"
                         "#database migration created TODO by yourself")]
      (check-dir? sql-path)
      (clojure.core/spit path init-content)
      path)
    (catch Exception ex
      (warn ex))))

(defn gen-test
  {:doc "auto generate clojure test file"}
  [project ^String ns-str]
  (try
   (let [file-name (-> ns-str
                       symbol
                       the-ns
                       namespace-munge)
         generate-ns (-> file-name
                         (clojure.string/replace  #"_" "-")
                         (str "-test"))
         target-dir (->>
                     (-> generate-ns
                         (clojure.string/replace #"-" "_")
                         (clojure.string/replace #"\." "/")
                         (str ".clj"))
                     (str test-path))
         content (reformat-string
                  (format ";;auto generate clojure test file\n(ns %s
                    (:require [clojure.test :refer :all]
                              [%s :refer :all]))

                    (deftest a-test
                       (testing \"FIXME, I fail.\"
                       (is (not= 0 1)))) "
                          generate-ns
                          ns-str))
         check-dir (-> target-dir
                       (subs 0 (-> target-dir
                                   (.lastIndexOf "/"))))]
     (check-dir? check-dir)
     (clojure.core/spit target-dir content)
     target-dir)
   (catch Exception ex
     (warn ex))))

(defn gen-clojure [project ^String ns-str]
  (try
    (let [clj-path (-> base
                       (str ns-str)
                       (clojure.string/replace #"-" "_")
                       (clojure.string/replace #"\." "/"))
          clj-content (reformat-string
                       (format ";;;auto generate clojure file TODO by yourself\n(ns %s)\n\n(defn %s \n\"I don't do a lot.\"\n [project & args]\n(println \"Hi!\"))"
                               ns-str
                               (subs ns-str (inc (.lastIndexOf ns-str ".")))))]
      (check-dir? (-> clj-path
                      (subs 0 (-> clj-path
                                  (.lastIndexOf "/")))))
      (clojure.core/spit (str clj-path ".clj") clj-content)
      (str clj-path ".clj"))
    (catch Exception ex
      (warn ex))))
