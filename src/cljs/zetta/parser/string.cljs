;; This file was generated with lein-dalap from
;;
;; src/clj/zetta/parser/string.clj @ Tue Aug 06 00:02:50 PDT 2013
;;
(ns zetta.parser.string (:refer-clojure :exclude [take take-while]) (:require [cljs.core :as core] [clojure.string :as str] [zetta.parser.core :refer [<$>]] [zetta.parser.seq :as pseq]))
(defn take-with "Parser that matches `n` items of input, but succeed only if the predicate\n  `pred` returns `true` on the parsed input. The matched input is returned\n  as a string." [n pred] (<$> str/join (pseq/take-with n pred)))
(defn take "Parser that matches exactly `n` items from input. Returnes the matched\n  input as a string." [n] (take-with n (constantly true)))
(defn string "Parses a sequence of items that identically match a given string `s`.\n   Returns the parsed string.  This parser consumes no input if it fails (even\n   with a partial match)." [s] (take-with (count s) (fn* [p1__1210#] (= s p1__1210#))))
(defn take-while "Parser that matches input as long as pred returns `true`, and return\n   the consumed input as a string.\n\n   This parser does not fail.  It will return an empty seq if the predicate\n   returns `false` on the first token of input.\n\n   **WARNING**: Because this parser does not fail, do not use it with combinators\n   such as `many`, because such parsers loop until a failure occurs. Careless\n   use will thus result in an infinite loop." [pred] (<$> str/join (pseq/take-while pred)))
(defn take-till "Parser that matches input as long as `pred` returns `false` (i.e. until it\n  returns `true`), and returns the consumed input as a seq.\n\n  This parser does not fail.  It will return an empty string if the\n  predicate returns `true` on the first item from the input.\n\n  **WARNING**: Because this parser does not fail, do not use it with combinators\n  such as `many`, because such parsers loop until a failure occurs. Careless\n  use will thus result in an infinite loop." [pred] (take-while (complement pred)))
(def take-rest "Parser that always success and returns the rest of the seqs that are given\n  to the parser, the result will be a seqs of strings where the number of seqs\n  from the first level will represent the number of times a\n  continuation was used to continue the parse process." (<$> (fn* [p1__1211#] (map str/join p1__1211#)) pseq/take-rest))
(defn take-while1 "Parser that matches input as long as pred returns `true`. This parser\n  returns the consumed input in a string.\n\n  This parser will fail if a first match is not accomplished." [pred] (<$> str/join (pseq/take-while1 pred)))