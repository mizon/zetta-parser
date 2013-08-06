;; This file was generated with lein-dalap from
;;
;; src/clj/zetta/parser/core.clj @ Tue Aug 06 00:02:50 PDT 2013
;;
(ns zetta.parser.core (:require-macros [monads.macros :as monad-macro]) (:require [monads.core :as monad]))
(defrecord ResultDone [remainder result])
(defrecord ResultFailure [remainder stack msg])
(def partial? fn?)
(defn done? "Returns true when the parser has successfuly parsed the\n  given input." [result] (instance? ResultDone result))
(defn failure? "Returns true when the parser has failed when parsing the\n  given input." [result] (instance? ResultFailure result))
(def complete :user/complete)
(def incomplete :user/incomplete)
(defn complete? "Test More flag is equal to the complete constant or not." [m] (= m complete))
(defn incomplete? "Test More flag is equal to the incomplete constant or not." [m] (= m incomplete))
(defn- p-trampoline "Exact copy of the `clojure.core/trampoline` function, however it checks if\n  the returned function has a :stop meta to return. This is used by the prompt\n  and parse functions." ([f] (let [ret (f)] (if (and (fn? ret) (-> ret meta :stop not)) (recur ret) ret))) ([f & args] (p-trampoline (fn* [] (apply f args)))))
(defn- concat-more "Merges two More flag values ('complete' and 'incomplete') and returns a\n  new flag value." [m1 m2] (cond (complete? m1) complete (complete? m2) complete :else incomplete))
(defn add-parser-stream "Concats the input and `more` flag from two different parsers and calls\n  the function f with the result." [input0 more0 input1 more1 f] (f (concat input0 input1) (concat-more more0 more1)))
(declare always)
(declare bind-parsers)
(declare fail-parser)
(declare join-parsers)
(deftype Parser [f] IFn (-invoke [this_ input0 more0 err-fn ok-fn] (f input0 more0 err-fn ok-fn)) monad/Monad (do-result [self a] (always a)) (bind [self f] (bind-parsers self f)) monad/MonadZero (zero [_] (fail-parser "MonadZero/zero")) (plus-step [self ps] (reduce join-parsers self ps)))
(defn fail-parser "Parser that will always fail, you may provide an error message msg that\n  will be shown on the final result." [msg] (Parser. (fn failed-parser [input0 more0 err-fn _ok-fn] (fn* [] (err-fn input0 more0 [] (str "Failed reading: " msg))))))
(defn always "Returns a parser that will always succeed, this parser will return the\n  parameter given." [a] (Parser. (fn new-parser [input0 more0 _err-fn ok-fn] (fn* [] (ok-fn input0 more0 a)))))
(defn bind-parsers "Receives a parser and a continuation function, the result of the parser is\n  going to be given as a parameter to the f function, and this function should\n  return a new parser.\n\n  Example:\n\n    ; Everytime we parse an 'a' character and return a \"hello\" string\n    (bind-parsers (char \\a) (fn [achr] (always \"hello\")))\n  " [p f] (Parser. (fn parser-continuation [input0 more0 err-fn ok-fn0] (letfn [(ok-fn [input1 more1 a] ((f a) input1 more1 err-fn ok-fn0))] (p input0 more0 err-fn ok-fn)))))
(defn join-parsers "Merges two parsers together and returns a new parser that will execute\n  parser p1, in case this fails, it is going to execute parser p2.\n\n  Example:\n\n    ; Parses either the character a or the character b\n    (join-parsers (char \\a) (char \\b))\n  " [p1 p2] (Parser. (fn m-plus-parser [input0 more0 err-fn0 ok-fn] (letfn [(err-fn [input1 more1 _ _] (p2 input1 more1 err-fn0 ok-fn))] (p1 input0 more0 err-fn ok-fn)))))
(def parser-monad always)
(def dummy-parser (parser-monad nil))
(defn- failure-fn "The initial `err-fn` for all parsers that are executed on the zetta-parser\n  library." [input0 _more0 stack msg] (ResultFailure. input0 stack msg))
(defn- success-fn "The initial `ok-fn` for all parsers that are executed on the zetta-parser\n  library." [input0 _more0 result] (ResultDone. input0 result))
(def prompt "This is parser is used to return continuations (when there is not\n  enough input available for the parser to either succeed or fail)." (Parser. (fn inner-prompt [input0 _more0 err-fn ok-fn] (with-meta (fn [new-input] (if (empty? new-input) (p-trampoline err-fn input0 complete) (p-trampoline ok-fn (concat input0 (seq new-input)) incomplete))) {:stop true}))))
(defn parse "Uses the given parser to process the input, this function may return a\n  result that could either be a success, a failure, or a continuation that\n  will require more input in other to finish. The parser continuation\n  will halt as soon as an empty seq is given." [parser input] (p-trampoline parser (seq input) incomplete failure-fn success-fn))
(defn parse-once "Uses the given parser to process the input, this may return a result that\n  could either be a success or failure result (All input must be available\n  at once when using this function)." [parser input] (let [result (parse parser input)] (if (partial? result) (result "") result)))
(def >>= "Alias for bind-parsers function." bind-parsers)
(defn <$> "Maps the function f to the results of the given parsers, applicative\n  functor result is going to be a parameter for function f.\n\n  Example:\n\n    (<$> + number number)\n\n  Where `number` is a parser that will return a number from the parsed input." [f & more] (bind-parsers (monad/seq always more) (fn functor-application [params] (always (apply f params)))))
(def <|> "Alias for join-parsers function." join-parsers)