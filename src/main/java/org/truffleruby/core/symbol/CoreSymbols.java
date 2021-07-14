/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.symbol;

import java.util.ArrayList;
import java.util.List;

import org.jcodings.specific.USASCIIEncoding;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.rope.LeafRope;
import org.truffleruby.core.rope.RopeConstants;
import org.truffleruby.core.rope.RopeOperations;

// GENERATED BY tool/generate-core-symbols.rb
// This file is automatically generated from tool/id.def with 'jt build core-symbols'

// @formatter:off
public class CoreSymbols {

    public static final long STATIC_SYMBOL_ID = 0x1;
    private static final long GLOBAL_SYMBOL_ID = (0x03 << 1);

    public static final int STATIC_SYMBOLS_SIZE = 237;

    public final List<RubySymbol> CORE_SYMBOLS = new ArrayList<>();
    public final RubySymbol[] STATIC_SYMBOLS = new RubySymbol[STATIC_SYMBOLS_SIZE];

    public final RubySymbol CLASS = createRubySymbol("class");
    public final RubySymbol IMMEDIATE = createRubySymbol("immediate");
    public final RubySymbol LINE = createRubySymbol("line");
    public final RubySymbol NEVER = createRubySymbol("never");
    public final RubySymbol ON_BLOCKING = createRubySymbol("on_blocking");
    public final RubySymbol DEPRECATED = createRubySymbol("deprecated");
    public final RubySymbol EXPERIMENTAL = createRubySymbol("experimental");
    public final RubySymbol BIG = createRubySymbol("big");
    public final RubySymbol LITTLE = createRubySymbol("little");
    public final RubySymbol NATIVE = createRubySymbol("native");

    public static final int FIRST_OP_ID = 33;

    public final RubySymbol BANG = createRubySymbol("!", 33);
    public final RubySymbol DOUBLE_QUOTE = createRubySymbol("\"", 34);
    public final RubySymbol POUND = createRubySymbol("#", 35);
    public final RubySymbol DOLLAR = createRubySymbol("$", 36);
    public final RubySymbol MODULO = createRubySymbol("%", 37);
    public final RubySymbol AMPERSAND = createRubySymbol("&", 38);
    public final RubySymbol SINGLE_QUOTE = createRubySymbol("'", 39);
    public final RubySymbol LPAREN = createRubySymbol("(", 40);
    public final RubySymbol RPAREN = createRubySymbol(")", 41);
    public final RubySymbol MULTIPLY = createRubySymbol("*", 42);
    public final RubySymbol PLUS = createRubySymbol("+", 43);
    public final RubySymbol COMMA = createRubySymbol(",", 44);
    public final RubySymbol MINUS = createRubySymbol("-", 45);
    public final RubySymbol PERIOD = createRubySymbol(".", 46);
    public final RubySymbol DIVIDE = createRubySymbol("/", 47);
    public final RubySymbol COLON = createRubySymbol(":", 58);
    public final RubySymbol SEMICOLON = createRubySymbol(";", 59);
    public final RubySymbol LESS_THAN = createRubySymbol("<", 60);
    public final RubySymbol EQUAL = createRubySymbol("=", 61);
    public final RubySymbol GREATER_THAN = createRubySymbol(">", 62);
    public final RubySymbol QUESTION_MARK = createRubySymbol("?", 63);
    public final RubySymbol AT_SYMBOL = createRubySymbol("@", 64);
    public final RubySymbol LEFT_BRACKET = createRubySymbol("[", 91);
    public final RubySymbol BACK_SLASH = createRubySymbol("\\", 92);
    public final RubySymbol RIGHT_BRACKET = createRubySymbol("]", 93);
    public final RubySymbol CIRCUMFLEX = createRubySymbol("^", 94);
    public final RubySymbol BACK_TICK = createRubySymbol("`", 96);
    public final RubySymbol LEFT_BRACE = createRubySymbol("{", 123);
    public final RubySymbol PIPE = createRubySymbol("|", 124);
    public final RubySymbol RIGHT_BRACE = createRubySymbol("}", 125);
    public final RubySymbol TILDE = createRubySymbol("~", 126);

    public final RubySymbol DOT2 = createRubySymbol("..", 128);
    public final RubySymbol DOT3 = createRubySymbol("...", 129);
    // Skipped duplicated operator: BDOT2 .. 130
    // Skipped duplicated operator: BDOT3 ... 131
    public final RubySymbol UPLUS = createRubySymbol("+@", 132);
    public final RubySymbol UMINUS = createRubySymbol("-@", 133);
    public final RubySymbol POW = createRubySymbol("**", 134);
    public final RubySymbol CMP = createRubySymbol("<=>", 135);
    public final RubySymbol LSHFT = createRubySymbol("<<", 136);
    public final RubySymbol RSHFT = createRubySymbol(">>", 137);
    public final RubySymbol LEQ = createRubySymbol("<=", 138);
    public final RubySymbol GEQ = createRubySymbol(">=", 139);
    public final RubySymbol EQ = createRubySymbol("==", 140);
    public final RubySymbol EQQ = createRubySymbol("===", 141);
    public final RubySymbol NEQ = createRubySymbol("!=", 142);
    public final RubySymbol MATCH = createRubySymbol("=~", 143);
    public final RubySymbol NMATCH = createRubySymbol("!~", 144);
    public final RubySymbol AREF = createRubySymbol("[]", 145);
    public final RubySymbol ASET = createRubySymbol("[]=", 146);
    public final RubySymbol COLON2 = createRubySymbol("::", 147);
    public final RubySymbol ANDOP = createRubySymbol("&&", 148);
    public final RubySymbol OROP = createRubySymbol("||", 149);
    public final RubySymbol ANDDOT = createRubySymbol("&.", 150);

    public final RubySymbol NILP = createRubySymbol("nil?", 151);
    public final RubySymbol NULL = createRubySymbol("", 152);
    public final RubySymbol EMPTYP = createRubySymbol("empty?", 153);
    public final RubySymbol EQLP = createRubySymbol("eql?", 154);
    public final RubySymbol RESPOND_TO = createRubySymbol("respond_to?", 155);
    public final RubySymbol RESPOND_TO_MISSING = createRubySymbol("respond_to_missing?", 156);
    public final RubySymbol IFUNC = createRubySymbol("<IFUNC>", 157);
    public final RubySymbol CFUNC = createRubySymbol("<CFUNC>", 158);
    public final RubySymbol CORE_SET_METHOD_ALIAS = createRubySymbol("core#set_method_alias", 159);
    public final RubySymbol CORE_SET_VARIABLE_ALIAS = createRubySymbol("core#set_variable_alias", 160);
    public final RubySymbol CORE_UNDEF_METHOD = createRubySymbol("core#undef_method", 161);
    public final RubySymbol CORE_DEFINE_METHOD = createRubySymbol("core#define_method", 162);
    public final RubySymbol CORE_DEFINE_SINGLETON_METHOD = createRubySymbol("core#define_singleton_method", 163);
    public final RubySymbol CORE_SET_POSTEXE = createRubySymbol("core#set_postexe", 164);
    public final RubySymbol CORE_HASH_MERGE_PTR = createRubySymbol("core#hash_merge_ptr", 165);
    public final RubySymbol CORE_HASH_MERGE_KWD = createRubySymbol("core#hash_merge_kwd", 166);
    public final RubySymbol CORE_RAISE = createRubySymbol("core#raise", 167);
    // Skipped preserved token: `_debug_created_info`

    public static final int LAST_OP_ID = 168;

    public final RubySymbol MAX = createRubySymbol("max", toLocal(169));
    public final RubySymbol MIN = createRubySymbol("min", toLocal(170));
    public final RubySymbol FREEZE = createRubySymbol("freeze", toLocal(171));
    public final RubySymbol INSPECT = createRubySymbol("inspect", toLocal(172));
    public final RubySymbol INTERN = createRubySymbol("intern", toLocal(173));
    public final RubySymbol OBJECT_ID = createRubySymbol("object_id", toLocal(174));
    public final RubySymbol CONST_MISSING = createRubySymbol("const_missing", toLocal(175));
    public final RubySymbol METHODMISSING = createRubySymbol("method_missing", toLocal(176));
    public final RubySymbol METHOD_ADDED = createRubySymbol("method_added", toLocal(177));
    public final RubySymbol SINGLETON_METHOD_ADDED = createRubySymbol("singleton_method_added", toLocal(178));
    public final RubySymbol METHOD_REMOVED = createRubySymbol("method_removed", toLocal(179));
    public final RubySymbol SINGLETON_METHOD_REMOVED = createRubySymbol("singleton_method_removed", toLocal(180));
    public final RubySymbol METHOD_UNDEFINED = createRubySymbol("method_undefined", toLocal(181));
    public final RubySymbol SINGLETON_METHOD_UNDEFINED = createRubySymbol("singleton_method_undefined", toLocal(182));
    public final RubySymbol LENGTH = createRubySymbol("length", toLocal(183));
    public final RubySymbol SIZE = createRubySymbol("size", toLocal(184));
    public final RubySymbol GETS = createRubySymbol("gets", toLocal(185));
    public final RubySymbol SUCC = createRubySymbol("succ", toLocal(186));
    public final RubySymbol EACH = createRubySymbol("each", toLocal(187));
    public final RubySymbol PROC = createRubySymbol("proc", toLocal(188));
    public final RubySymbol LAMBDA = createRubySymbol("lambda", toLocal(189));
    public final RubySymbol SEND = createRubySymbol("send", toLocal(190));
    public final RubySymbol __SEND__ = createRubySymbol("__send__", toLocal(191));
    public final RubySymbol __ATTACHED__ = createRubySymbol("__attached__", toLocal(192));
    public final RubySymbol INITIALIZE = createRubySymbol("initialize", toLocal(193));
    public final RubySymbol INITIALIZE_COPY = createRubySymbol("initialize_copy", toLocal(194));
    public final RubySymbol INITIALIZE_CLONE = createRubySymbol("initialize_clone", toLocal(195));
    public final RubySymbol INITIALIZE_DUP = createRubySymbol("initialize_dup", toLocal(196));
    public final RubySymbol TO_INT = createRubySymbol("to_int", toLocal(197));
    public final RubySymbol TO_ARY = createRubySymbol("to_ary", toLocal(198));
    public final RubySymbol TO_STR = createRubySymbol("to_str", toLocal(199));
    public final RubySymbol TO_SYM = createRubySymbol("to_sym", toLocal(200));
    public final RubySymbol TO_HASH = createRubySymbol("to_hash", toLocal(201));
    public final RubySymbol TO_PROC = createRubySymbol("to_proc", toLocal(202));
    public final RubySymbol TO_IO = createRubySymbol("to_io", toLocal(203));
    public final RubySymbol TO_A = createRubySymbol("to_a", toLocal(204));
    public final RubySymbol TO_S = createRubySymbol("to_s", toLocal(205));
    public final RubySymbol TO_I = createRubySymbol("to_i", toLocal(206));
    public final RubySymbol TO_F = createRubySymbol("to_f", toLocal(207));
    public final RubySymbol TO_R = createRubySymbol("to_r", toLocal(208));
    public final RubySymbol BT = createRubySymbol("bt", toLocal(209));
    public final RubySymbol BT_LOCATIONS = createRubySymbol("bt_locations", toLocal(210));
    public final RubySymbol CALL = createRubySymbol("call", toLocal(211));
    public final RubySymbol MESG = createRubySymbol("mesg", toLocal(212));
    public final RubySymbol EXCEPTION = createRubySymbol("exception", toLocal(213));
    public final RubySymbol LOCALS = createRubySymbol("locals", toLocal(214));
    public final RubySymbol NOT = createRubySymbol("not", toLocal(215));
    public final RubySymbol AND = createRubySymbol("and", toLocal(216));
    public final RubySymbol OR = createRubySymbol("or", toLocal(217));
    public final RubySymbol DIV = createRubySymbol("div", toLocal(218));
    public final RubySymbol DIVMOD = createRubySymbol("divmod", toLocal(219));
    public final RubySymbol FDIV = createRubySymbol("fdiv", toLocal(220));
    public final RubySymbol QUO = createRubySymbol("quo", toLocal(221));
    public final RubySymbol NAME = createRubySymbol("name", toLocal(222));
    public final RubySymbol NIL = createRubySymbol("nil", toLocal(223));
    public final RubySymbol USCORE = createRubySymbol("_", toLocal(224));
    public final RubySymbol NUMPARAM_1 = createRubySymbol("_1", toLocal(225));
    public final RubySymbol NUMPARAM_2 = createRubySymbol("_2", toLocal(226));
    public final RubySymbol NUMPARAM_3 = createRubySymbol("_3", toLocal(227));
    public final RubySymbol NUMPARAM_4 = createRubySymbol("_4", toLocal(228));
    public final RubySymbol NUMPARAM_5 = createRubySymbol("_5", toLocal(229));
    public final RubySymbol NUMPARAM_6 = createRubySymbol("_6", toLocal(230));
    public final RubySymbol NUMPARAM_7 = createRubySymbol("_7", toLocal(231));
    public final RubySymbol NUMPARAM_8 = createRubySymbol("_8", toLocal(232));
    public final RubySymbol NUMPARAM_9 = createRubySymbol("_9", toLocal(233));
    public final RubySymbol LASTLINE = createRubySymbol("$_", toGlobal(234));
    public final RubySymbol BACKREF = createRubySymbol("$~", toGlobal(235));
    public final RubySymbol ERROR_INFO = createRubySymbol("$!", toGlobal(236));

    public RubySymbol createRubySymbol(String string, long id) {
        LeafRope rope = RopeConstants.lookupUSASCII(string);
        if (rope == null) {
            rope = RopeOperations.encodeAscii(string, USASCIIEncoding.INSTANCE);
        }

        final RubySymbol symbol = new RubySymbol(string, rope, Encodings.US_ASCII, id);
        CORE_SYMBOLS.add(symbol);

        if (id != RubySymbol.UNASSIGNED_ID) {
            final int index = idToIndex(id);
            STATIC_SYMBOLS[index] = symbol;
        }
        return symbol;
    }

    public RubySymbol createRubySymbol(String string) {
        return createRubySymbol(string, RubySymbol.UNASSIGNED_ID);
    }

    public static int idToIndex(long id) {
      final int index;
      if (id > LAST_OP_ID) {
        index = (int) id >> 4;
      } else {
        index = (int) id;
      }
      assert index < STATIC_SYMBOLS_SIZE;
      return index;
    }

    private static long toLocal(long id) {
        return id << 4 | STATIC_SYMBOL_ID;
    }

    private static long toGlobal(long id) {
        return id << 4 | STATIC_SYMBOL_ID | GLOBAL_SYMBOL_ID;
    }

    public static boolean isStaticSymbol(long value) {
        return (value >= FIRST_OP_ID && value <= LAST_OP_ID) ||
                ((value & STATIC_SYMBOL_ID) == STATIC_SYMBOL_ID && (value >> 4) < STATIC_SYMBOLS_SIZE);
    }

}
// @formatter:on
