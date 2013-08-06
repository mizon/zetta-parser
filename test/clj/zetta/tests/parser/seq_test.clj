(ns zetta.tests.parser.seq-test
  ^:cljs-macro
  (:require
   ^{:cljs [buster-cljs.macros :refer [initialize-buster deftest it is]]}
   [buster-cljs.clojure :refer [deftest it is]])
  (:require
   #_(:cljs [goog.string :as _gstring])
   [zetta.parser.core :refer [parse-once failure? done?]]
   [zetta.parser.seq :as p]))

#_(:cljs (initialize-buster))

(defn is-digit
  [n]
  ^{:cljs '(goog.string.isNumeric n)}
  (Character/isDigit ^java.lang.Character n))

(deftest seq-parser

  (it "satisfy?"
    (let [result (parse-once (p/satisfy? #(= 0 (mod % 2)))
                             [10])]
      (is (done? result))
      (is (= 10 (:result result)))))

  (it "satisfy? no initial match"
    (let [result (parse-once (p/satisfy? #(= 0 (mod % 2)))
                             [5])]
      (is (failure? result))))

  (it "skip"
      (let [result (parse-once (p/skip is-digit)
                             "432")]
      (is (nil? (:result result)))
      (is (= [\3 \2] (:remainder result)))))

  (it "skip no initial match"
    (let [result (parse-once (p/skip is-digit)
                             "hello")]
      (is (failure? result))))

  (it "take-with"
    (let [result (parse-once
                  (p/take-with 4 (partial every?
                                          is-digit))
                  "12345")]
      (is (= [\1 \2 \3 \4] (:result result)))))

  (it "take with no initial match"
    (let [result (parse-once
                  (p/take-with 4
                               (partial every?
                                        is-digit))
                  "12ab3")]
      (is (failure? result))))


  (it "take"
    (let [result (parse-once
                  (p/take 5)
                  "12ab hell")]
      (is (= [\1 \2 \a \b \space] (:result result)))))

  (it "take no initial match"
    (let [result (parse-once
                  (p/take 5)
                  "12ab")]
      (is (failure? result))))


  (it "string"
    (let [result (parse-once
                  (p/string "hello")
                  "hello world")]
      (is (= "hello" (:result result)))
      (is (= (seq " world") (:remainder result)))))


  (it "string no initial match"
    (let [result (parse-once
                  (p/string "hello")
                  "other world")]
      (is (failure? result))
      (is (= (seq "other world") (:remainder result)))))

  (it "skip-while"
    (let [result (parse-once
                  (p/skip-while
                   ^{:cljs '#(goog.string.isBreakingWhitespace %)}
                   #(Character/isWhitespace ^java.lang.Character %))
                  "    \tanother test")]
      (is (done? result))
      (is (= (seq "another test") (:remainder result)))))

  (it "skip while no initial match"
    (let [result (parse-once
                  (p/skip-while
                   ^{:cljs '#(goog.string.isBreakingWhitespace %)}
                   #(Character/isWhitespace ^java.lang.Character %))
                  "another test")]
      (is (done? result))
      (is (= (seq "another test") (:remainder result)))))

  (it "take-while"
    (let [result (parse-once
                  (p/take-while
                   ^{:cljs '#(goog.string.isAlpha %)}
                   #(Character/isLetter ^java.lang.Character %))
                  "this is just a test")]
      (is (= (seq "this") (:result result)))
      (is (= (seq " is just a test") (:remainder result)))))

  (it "take while no initial match"
    (let [result (parse-once
                  (p/take-while
                   ^{:cljs '#(goog.string.isAlpha %)}
                   #(Character/isLetter ^java.lang.Character %))
                  " this is just a test")]
      (is (done? result))
      (is (= [] (:result result)))
      (is (= (seq " this is just a test") (:remainder result)))))

  (it "take-till"
    (let [result (parse-once
                  (p/take-till #(= % \space))
                  "this is just a test")]
      (is (done? result))
      (is (= (seq "this") (:result result)))
      (is (= (seq " is just a test") (:remainder result)))))

  (it "take till no initial match"
    (let [result (parse-once
                  (p/take-till #(= % \space))
                  " this is just a test")]
      (is (done? result))
      (is (= [] (:result result)))
      (is (= (seq " this is just a test") (:remainder result)))))

  (it "take-rest"
    (let [result (parse-once p/take-rest "hello world")]
      (is (done? result))
      (is (= [(seq "hello world")] (:result result)))))

  (it "take-while1"
    (let [result (parse-once
                  (p/take-while1
                   ^{:cljs '#(goog.string.isAlpha %)}
                   #(Character/isLetter ^java.lang.Character %))
                  "this is just a test")]
      (is (= (seq "this") (:result result)))
      (is (= (seq " is just a test") (:remainder result)))))

  (it "take while1 no initial match"
    (let [result (parse-once
                  (p/take-while1
                   ^{:cljs '#(goog.string.isAlpha %)}
                   #(Character/isLetter ^java.lang.Character %))
                  " this is just a test")]
      (is (failure? result))))

  (it "any-token"
    (let [result (parse-once p/any-token "1bc")]
      (is (done? result))
      (is (= \1 (:result result)))))

  (it "char"
    (let [result (parse-once (p/char \a) "abc")]
      (is (done? result))
      (is (= \a (:result result)))))

  (it "char no initial match"
    (let [result (parse-once (p/char \a) "1bc")]
      (is (failure? result))
      (is (= (seq "1bc") (:remainder result)))))

  (it "char set"
    (let [result (parse-once (p/char #{\,}) ",")]
      (is (done? result))))

  (it "no char set"
    (let [result (parse-once (p/not-char #{\,}) ",")]
      (is (failure? result))))

  (it "test double or long"
    (let [result (parse-once p/double-or-long "123")]
      (is (done? result))
      (is (= (seq "123") (:result result))))
    (let [result (parse-once p/double-or-long "1.23")]
      (is (done? result))
      (is (= (seq "1.23") (:result result)))))

  (it "number small"
    (let [result (parse-once p/number "1")]
      (is (done? result))
      (is (= 1 (:result result)))
      ^:clj (is (= java.lang.Long (type (:result result)))))
    (let [result (parse-once p/number "123")]
      (is (done? result))
      (is (= 123 (:result result)))
      ^:clj (is (= java.lang.Long (type (:result result))))))

  ^:clj
  (it "number big"
    (let [result (parse-once p/number "11111111111111111111")]
      (is (done? result))
      (is (= 11111111111111111111 (:result result)))
      (is (= clojure.lang.BigInt (type (:result result))))))

  (it "number-double"
    (let [result (parse-once p/number "3.1415")]
      (is (done? result))
      (is (= 3.1415 (:result result)))
      ^:clj (is (= java.lang.Double (type (:result result))))))

  (it "number no initial match"
    (let [result (parse-once p/number "john doe")]
      (is (failure? result))))

  (it "end-of-input"
    (let [result (parse-once p/end-of-input "")]
      (is (done? result))))

  (it "end-of-input-no-initial-match"
    (let [result (parse-once p/end-of-input "hello")]
      (is (failure? result))))

  (it "at-end?"
    (let [result1 (parse-once p/at-end? "")
          result2 (parse-once p/at-end? "hello")]
      (is (:result result1))
      (is (not (:result result2)))))

  (it "eol"
    (let [result1 (parse-once p/eol "\n")
          result2 (parse-once p/eol "\r\n")]
      (is (done? result1))
      (is (done? result2))))

  (it "eol no initial match"
    (let [result (parse-once p/eol "other")]
      (is (failure? result)))))