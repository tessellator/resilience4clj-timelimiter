## 2. Usage

### Executing Code Protected by a Time Limiter

There are two ways to execute code to be protected by the time limiter:
`execute` and `with-time-limiter`.

`execute` executes a single function within the context of the time limiter
and applies any args to it. If the execution of the function exceeds the timeout
duration, an exception is thrown.

```clojure
> (require '[resilience4clj.time-limiter :as tl])
;; => nil

> (cb/execute (tl/time-limiter! :my-limiter) map inc [1 2 3])
;; => (2 3 4) if the execution is shorter than the timeout duration
;;    OR
;;    throws an exception if the execution is longer than the timeout duration
```

`execute` is rather low-level. To make execution more convenient, this library
also includes a `with-time-limiter` macro that executes several forms within
a context protected by the time limiter. When you use the macro, you must
either provide a time limiter or the name of one in the global registry. If
you provide a name and a time limiter of that name does not already exist in
the global registry, one is created with the `:default` config.

```clojure
> (require '[resilience4clj.time-limiter :refer [with-time-limiter]])
;; => nil

> (with-time-limiter :my-limiter
    (my-long-running-function)
    ;; other code here
  )
;; => some value if the execution of the body inside the macro is shorter than
;;    the timeout duration, else throws an exception
```
