(ns reply.eval-modes.shared
  (:require
    [clojure.main]
    [reply.conversions :refer [->fn]]
    [reply.reader.simple-jline :as simple-jline]))

(def colored-output
  {:print-err #(binding [*out* *err*]
                 (print "\033[31m")
                 (print %)
                 (print "\033[m")
                 (flush))
   :print-out print
   :print-value (fn [x]
                  (print "\033[34m")
                  (print x)
                  (print "\033[m")
                  (flush))})

(defn set-default-options [options]
  (let [options (assoc options :prompt (->fn (:custom-prompt options)
                                             (fn [ns] (str ns "=> "))))
        options (assoc options :subsequent-prompt (->fn (:subsequent-prompt options)
                                                        (constantly nil)))
        options (assoc options :print-value (->fn (:print-value options)
                                                  print))
        options (if (:color options)
                  (merge options colored-output)
                  options)
        options (assoc options :read-input-line-fn
                       (fn []
                         (simple-jline/safe-read-line
                           {:no-jline true
                            :prompt-string ""})))]
    options))

(defn load-parsed-forms-fn-in-background
  "Loading of reply.parsing namespace takes a lot of time. We can do it in
  background while the user is typing their first form, that way user spends
  less time waiting.

  Return the future that delivers `reply.parsing/parsed-forms` function."
  []
  (future (require 'reply.parsing)
          (resolve 'reply.parsing/parsed-forms)))
