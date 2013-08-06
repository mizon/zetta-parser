;; This file was generated with lein-dalap from
;;
;; src/clj/zetta/parser/seq.clj @ Tue Aug 06 00:02:50 PDT 2013
;;
(ns zetta.parser.seq (:refer-clojure :exclude [ensure get take take-while char some replicate]) (:require-macros [zetta.parser.macros :as zmacro]) (:require [cljs.core :as c] [clojure.string :as str] [goog.string :as _gstring] [monads.core :as monad] [zetta.parser.core :refer [Parser]] [zetta.parser.core :as zcore] [zetta.parser.combinators :as zcomb]))
(defn- span [pred xs] ((c/juxt (fn* [p1__1196#] (c/take-while pred p1__1196#)) (fn* [p1__1197#] (c/drop-while pred p1__1197#))) xs))
(def demand-input "Basic parser that will ensure the request of more input via a continuation." (Parser. (fn -inner-demmand-input [input0 more0 err-fn0 ok-fn0] (if (zcore/complete? more0) (fn* [] (err-fn0 input0 more0 ["demand-input"] "not enough input")) (letfn [(err-fn [input more] (fn* [] (err-fn0 input more ["demand-input"] "not enough input"))) (ok-fn [input more] (fn* [] (ok-fn0 input more nil)))] (zcore/prompt input0 more0 err-fn ok-fn))))))
(def want-input? "Parser that returns `true` if any input is available either immediately or\n  on demand, and `false` if the end of all input has been reached.\n\n  **WARNING**: This parser always succeeds." (Parser. (fn -inner-want-input? [input0 more0 _err-fn ok-fn0] (cond (not (empty? input0)) (fn* [] (ok-fn0 input0 more0 true)) (zcore/complete? more0) (fn* [] (ok-fn0 input0 more0 false)) :else (letfn [(err-fn [input more] (fn* [] (ok-fn0 input more false))) (ok-fn [input more] (fn* [] (ok-fn0 input more true)))] (zcore/prompt input0 more0 err-fn ok-fn))))))
(defn ensure "If at least `n` items of input are available, return the current input,\n  otherwise fail." [n] (Parser. (fn -inner-ensure [input0 more0 err-fn ok-fn] (if (>= (count input0) n) (fn* [] (ok-fn input0 more0 input0)) ((zmacro/>> demand-input (ensure n)) input0 more0 err-fn ok-fn)))))
(def get "Returns the input given in the `zetta.parser.core/parse` function." (Parser. (fn -inner-get [input0 more0 _err-fn ok-fn] (fn* [] (ok-fn input0 more0 input0)))))
(defn put [s] "Sets a (possibly modified) input into the parser state." (Parser. (fn -inner-put [_input0 more0 _err-fn ok-fn] (fn* [] (ok-fn s more0 nil)))))
(defn satisfy? "Parser that succeeds for any item for which the predicate `pred` returns\n  `true`. Returns the item that is actually parsed." [pred] (zmacro/do [input (ensure 1) :let [item (first input)] :if (pred item) :then [_ (put (rest input))] :else [_ (zcore/fail-parser "satisfy?")]] item))
(defn skip "Parser that succeeds for any item for which the predicate `pred`, returns\n  `nil`." [pred] (zmacro/do [input (ensure 1) :if (pred (first input)) :then [_ (put (rest input))] :else [_ (zcore/fail-parser "skip")]] nil))
(defn take-with "Parser that matches `n` items of input, but succeed only if the predicate\n  `pred` returns `true` on the parsed input. The matched input is returned\n  as a seq." [n pred] (zmacro/do [input (ensure n) :let [[h t] (split-at n input)] :if (pred h) :then [_ (put t)] :else [_ (zcore/fail-parser "take-with")]] h))
(defn take "Parser that matches exactly `n` items from input. Returnes the matched\n  input as a seq." [n] (take-with n (constantly true)))
(defn string "Parses a sequence of items that identically match a given string `s`.\n  Returns the parsed string. This parser consumes no input if it fails (even\n  with a partial match)." [s] (let [ch-vec (vec s)] (zcore/<$> str/join (take-with (count s) (fn* [p1__1198#] (= ch-vec p1__1198#))))))
(defn skip-while "Parser that skips input for as long as `pred` returns `true`." [pred] (let [skip-while-loop (zmacro/do [input0 get :let [input (drop-while pred input0)] _ (put input) :if (empty? input) :then [input-available? want-input? :if input-available? :then [_ (skip-while pred)] :else []] :else []] nil)] skip-while-loop))
(defn take-while "Parser that matches input as long as pred returns `true`, and return\n   the consumed input as a seq.\n\n   This parser does not fail.  It will return an empty seq if the predicate\n   returns `false` on the first token of input.\n\n   **WARNING**: Because this parser does not fail, do not use it with\n   combinators such as `many`, because such parsers loop until a failure\n   occurs. Careless use will thus result in an infinite loop." [pred] (letfn [(take-while-loop [acc] (zmacro/do [input0 get :let [[pre post] (span pred input0)] _ (put post) :if (empty? post) :then [input-available? want-input? :if input-available? :then [result (take-while-loop (conj acc pre))] :else [result (zcore/always (conj acc pre))]] :else [result (monad/do-result zcore/dummy-parser (conj acc pre))]] result))] (zcore/<$> (comp (fn* [p1__1199#] (apply c/concat p1__1199#)) c/reverse) (take-while-loop []))))
(defn take-till "Matches input as long as `pred` returns `false`\n  (i.e. until it returns `true`), and returns the consumed input as a seq.\n\n  This parser does not fail.  It will return an empty seq if the predicate\n  returns `true` on the first item from the input.\n\n  **WARNING**: Because this parser does not fail, do not use it with combinators\n  such as `many`, because such parsers loop until a failure occurs. Careless\n  use will thus result in an infinite loop." [pred] (take-while (complement pred)))
(def take-rest "Parser that returns the rest of the seqs that are given to the parser,\n  the result will be a seqs of seqs where the number of seqs\n  from the first level will represent the number of times a\n  continuation was used to continue the parse process." (letfn [(take-rest-loop [acc] (zmacro/do [input-available? want-input? :if input-available? :then [input get _ (put []) result (take-rest-loop (conj acc input))] :else [result (zcore/always (reverse acc))]] result))] (take-rest-loop [])))
(defn take-while1 "Parser that matches input as long as pred returns `true`. This parser\n   returns the consumed input in a seq.\n\n   This parser will fail if a first match is not accomplished." [pred] (zmacro/do [input get :if (empty? input) :then [_ demand-input] :else [] input get :let [[pre post] (span pred input)] :if (empty? pre) :then [_ (zcore/fail-parser "take-while1")] :else [_ (put post)] :if (empty? post) :then [remainder (take-while pred) result (zcore/always (concat pre remainder))] :else [result (zcore/always pre)]] result))
(def any-token "Parser that matches any element from the input seq, it will return the\n  parsed element from the seq." (satisfy? (constantly true)))
(defn char "Parser that matches only a token that is equal to character `c`, the\n  character is returned." [c] (cond (set? c) (zcomb/<?> (satisfy? (fn* [p1__1200#] (contains? c p1__1200#))) (str "failed parser char: " c)) :else (zcomb/<?> (satisfy? (fn* [p1__1201#] (= p1__1201# c))) (str "failed parser char: " c))))
(def whitespace "Parser that matches any character that is considered a whitespace, it uses\n  `Character/isWhitespace` internally. This parser returns the whitespace\n  character." (satisfy? (fn* [p1__1202#] (goog.string.isBreakingWhitespace p1__1202#))))
(def space "Parser that matches any character that is equal to the character `\\space`.\n  This parser returns the `\\space` character." (char \space))
(def spaces "Parser that matches many spaces. Returns a seq of space characters" (zcomb/many space))
(def skip-spaces "Parser that skips many spaces. Returns `nil`." (zcomb/skip-many space))
(def skip-whitespaces "Parser that skips many whitespaces. Returns `nil`." (zcomb/skip-many whitespace))
(defn not-char "Parser that matches only an item that is not equal to character `c`, the\n  item is returned." [c] (cond (set? c) (zcomb/<?> (satisfy? (fn* [p1__1204#] (not (contains? c p1__1204#)))) (str c)) :else (zcomb/<?> (satisfy? (fn* [p1__1205#] (not (= p1__1205# c)))) (str c))))
(def letter "Parser that matches any character that is considered a letter, it uses\n  `Character/isLetter` internally. This parser will return the matched\n  character." (satisfy? (fn* [p1__1206#] (goog.string.isAlpha p1__1206#))))
(def word "Parser that matches a word, e.g `(many1 letter)`, returns the parsed word." (zcore/<$> str/join (zcomb/many1 letter)))
(def digit "Parser that matches any character that is considered a digit, it uses\n  `Character/isDigit` internally. This parser will return the matched digit\n  character." (satisfy? (fn* [p1__1208#] (goog.string.isNumeric p1__1208#))))
(defn- read-number [s] (if (> (.indexOf s ".") 0) (js/parseFloat s) (js/parseInt s 10)))
(def double-or-long (letfn [(digit-or-dot [] (zmacro/do [h (zcore/<|> digit (char \.)) :if (= h \.) :then [t (zcomb/many digit)] :else [t (zcore/<|> (digit-or-dot) (zcore/always []))]] (cons h t)))] (zmacro/do [h digit t (zcore/<|> (digit-or-dot) (zcore/always []))] (cons h t))))
(def number "Parser that matches one or more digit characters and returns a number in\n  base 10." (zcore/<$> (comp read-number str/join) double-or-long))
(def end-of-input "Parser that matches only when the end-of-input has been reached, otherwise\n  it fails. Returns a nil value." (Parser. (fn inner-end-of-input [input0 more0 err-fn0 ok-fn0] (if (empty? input0) (if (zcore/complete? more0) (fn* [] (ok-fn0 input0 more0 nil)) (letfn [(err-fn [input1 more1 _ _] (zcore/add-parser-stream input0 more0 input1 more1 (fn [input2 more2] (ok-fn0 input2 more2 nil)))) (ok-fn [input1 more1 _] (zcore/add-parser-stream input0 more0 input1 more1 (fn [input2 more2] (err-fn input2 more2 [] "end-of-input"))))] (demand-input input0 more0 err-fn ok-fn))) (fn* [] (err-fn0 input0 more0 [] "end-of-input"))))))
(def at-end? "Parser that never fails, it returns `true` when the end-of-input\n  is reached, `false` otherwise." (zcore/<$> not want-input?))
(def eol "Parser that matches different end-of-line characters/sequences.\n  This parser returns a nil value." (zcore/<|> (zmacro/*> (char \newline) (zcore/always nil)) (zmacro/*> (string "\r\n") (zcore/always nil))))