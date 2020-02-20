(ns resilience4clj.time-limiter
  "Functions to create and execute time limiters."
  (:refer-clojure :exclude [name])
  (:require [clojure.spec.alpha :as s])
  (:import [io.github.resilience4j.timelimiter
            TimeLimiter
            TimeLimiterConfig
            TimeLimiterRegistry]
           [java.time Duration]
           [java.util.function Supplier]))

;; -----------------------------------------------------------------------------
;; Time limiter configuration

(s/def ::timeout-duration
  nat-int?)

(s/def ::cancel-running-future
  boolean?)

(s/def ::config
  (s/keys :opt-un [::timeout-duration
                   ::cancel-running-future]))

(s/def ::name
  (s/or :string (s/and string? not-empty)
        :keyword keyword?))

(defn- build-config [config]
  (let [{:keys [timeout-duration
                cancel-running-future]} config]
    (cond-> (TimeLimiterConfig/custom)

      timeout-duration
      (.timeoutDuration (Duration/ofMillis timeout-duration))

      cancel-running-future
      (.cancelRunningFuture cancel-running-future)

      :always
      (.build))))

;; -----------------------------------------------------------------------------
;; Registry

(def registry
  "The global time limiter and config registry."
  (TimeLimiterRegistry/ofDefaults))

(defn- build-configs-map [configs-map]
  (into {} (map (fn [[k v]] [(clojure.core/name k) (build-config v)]) configs-map)))

(defn configure-registry!
  "Overwrites the global registry with one that contains the configs-map.

  configs-map is a map whose keys are names and vals are configs. When a time
  limiter is created, you may specify one of the names in this map to use as the
  config for that time limiter.

  :default is a special name. It will be used as the config for time limiters
  that do not specify a config to use."
  [configs-map]
  (alter-var-root (var registry)
                  (fn [_]
                    (TimeLimiterRegistry/of (build-configs-map configs-map)))))

;; -----------------------------------------------------------------------------
;; Creation and fetching from registry

(defn time-limiter!
  "Creates or fetches a time limiter with the specified name and config and
  stores it in the global registry.

  The config value can be either a config map or the name of a config map stored
  in the global registry.

  If the time limiter already exists in the global registry, the config value is
  ignored."
  ([name]
   {:pre [(s/assert ::name name)]}
   (.timeLimiter registry (clojure.core/name name)))
  ([name config]
   {:pre [(s/assert ::name name)
          (s/assert (s/or :name ::name :config ::config) config)]}
   (if (s/valid? ::name config)
     (.timeLimiter registry (clojure.core/name name) config)
     (.timeLimiter registry (clojure.core/name name) (build-config config)))))

(defn time-limiter
  "Creates a time limiter with the specified name and config."
  [name config]
  {:pre [(s/assert ::name name)
         (s/assert ::config config)]}
  (TimeLimiter/of (clojure.core/name name) (build-config config)))

;; -----------------------------------------------------------------------------
;; Execution

(defn execute
  "Applies args to f within a context protected by the time limiter.

  If the execution time exceeds the timeout duration, an exception is thrown."
  [^TimeLimiter time-limiter f & args]
  (.executeFutureSupplier time-limiter
                          (reify Supplier (get [_] (future (apply f args))))))

(defmacro with-time-limiter
  "Executes body within a context protected by the time limiter.

  If the execution time exceeds the timeout duration, an exception is thrown.

  `time-limiter` is either a time limiter or the name of one in the global
  registry. If you provide a name and a time limiter of that name does not
  already exist in the global registry, one will be created with the `:default`
  config."
  [time-limiter & body]
  `(let [tl# (if (s/valid? ::name ~time-limiter)
               (time-limiter! (clojure.core/name ~time-limiter))
               ~time-limiter)]
     (execute tl# (fn [] ~@body))))

;; -----------------------------------------------------------------------------
;; Time limiter properties

(defn name
  "Gets the name of the time limiter."
  [^TimeLimiter time-limiter]
  (.getName time-limiter))
