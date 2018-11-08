// Compiled by ClojureScript 1.10.339 {}
goog.provide('re_frame.fx');
goog.require('cljs.core');
goog.require('re_frame.router');
goog.require('re_frame.db');
goog.require('re_frame.interceptor');
goog.require('re_frame.interop');
goog.require('re_frame.events');
goog.require('re_frame.registrar');
goog.require('re_frame.loggers');
goog.require('re_frame.trace');
re_frame.fx.kind = new cljs.core.Keyword(null,"fx","fx",-1237829572);
if(cljs.core.truth_(re_frame.registrar.kinds.call(null,re_frame.fx.kind))){
} else {
throw (new Error("Assert failed: (re-frame.registrar/kinds kind)"));
}
/**
 * Register the given effect `handler` for the given `id`.
 * 
 *   `id` is keyword, often namespaced.
 *   `handler` is a side-effecting function which takes a single argument and whose return
 *   value is ignored.
 * 
 *   Example Use
 *   -----------
 * 
 *   First, registration ... associate `:effect2` with a handler.
 * 
 *   (reg-fx
 *   :effect2
 *   (fn [value]
 *      ... do something side-effect-y))
 * 
 *   Then, later, if an event handler were to return this effects map ...
 * 
 *   {...
 * :effect2  [1 2]}
 * 
 * ... then the `handler` `fn` we registered previously, using `reg-fx`, will be
 * called with an argument of `[1 2]`.
 */
re_frame.fx.reg_fx = (function re_frame$fx$reg_fx(id,handler){
return re_frame.registrar.register_handler.call(null,re_frame.fx.kind,id,handler);
});
/**
 * An interceptor whose `:after` actions the contents of `:effects`. As a result,
 *   this interceptor is Domino 3.
 * 
 *   This interceptor is silently added (by reg-event-db etc) to the front of
 *   interceptor chains for all events.
 * 
 *   For each key in `:effects` (a map), it calls the registered `effects handler`
 *   (see `reg-fx` for registration of effect handlers).
 * 
 *   So, if `:effects` was:
 *    {:dispatch  [:hello 42]
 *     :db        {...}
 *     :undo      "set flag"}
 * 
 *   it will call the registered effect handlers for each of the map's keys:
 *   `:dispatch`, `:undo` and `:db`. When calling each handler, provides the map
 *   value for that key - so in the example above the effect handler for :dispatch
 *   will be given one arg `[:hello 42]`.
 * 
 *   You cannot rely on the ordering in which effects are executed.
 */
re_frame.fx.do_fx = re_frame.interceptor.__GT_interceptor.call(null,new cljs.core.Keyword(null,"id","id",-1388402092),new cljs.core.Keyword(null,"do-fx","do-fx",1194163050),new cljs.core.Keyword(null,"after","after",594996914),(function re_frame$fx$do_fx_after(context){
if(re_frame.trace.is_trace_enabled_QMARK_.call(null)){
var _STAR_current_trace_STAR_1761 = re_frame.trace._STAR_current_trace_STAR_;
re_frame.trace._STAR_current_trace_STAR_ = re_frame.trace.start_trace.call(null,new cljs.core.PersistentArrayMap(null, 1, [new cljs.core.Keyword(null,"op-type","op-type",-1636141668),new cljs.core.Keyword("event","do-fx","event/do-fx",1357330452)], null));

try{try{var seq__1762 = cljs.core.seq.call(null,new cljs.core.Keyword(null,"effects","effects",-282369292).cljs$core$IFn$_invoke$arity$1(context));
var chunk__1763 = null;
var count__1764 = (0);
var i__1765 = (0);
while(true){
if((i__1765 < count__1764)){
var vec__1766 = cljs.core._nth.call(null,chunk__1763,i__1765);
var effect_key = cljs.core.nth.call(null,vec__1766,(0),null);
var effect_value = cljs.core.nth.call(null,vec__1766,(1),null);
var temp__5455__auto___1782 = re_frame.registrar.get_handler.call(null,re_frame.fx.kind,effect_key,false);
if(cljs.core.truth_(temp__5455__auto___1782)){
var effect_fn_1783 = temp__5455__auto___1782;
effect_fn_1783.call(null,effect_value);
} else {
re_frame.loggers.console.call(null,new cljs.core.Keyword(null,"error","error",-978969032),"re-frame: no handler registered for effect:",effect_key,". Ignoring.");
}


var G__1784 = seq__1762;
var G__1785 = chunk__1763;
var G__1786 = count__1764;
var G__1787 = (i__1765 + (1));
seq__1762 = G__1784;
chunk__1763 = G__1785;
count__1764 = G__1786;
i__1765 = G__1787;
continue;
} else {
var temp__5457__auto__ = cljs.core.seq.call(null,seq__1762);
if(temp__5457__auto__){
var seq__1762__$1 = temp__5457__auto__;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__1762__$1)){
var c__4351__auto__ = cljs.core.chunk_first.call(null,seq__1762__$1);
var G__1788 = cljs.core.chunk_rest.call(null,seq__1762__$1);
var G__1789 = c__4351__auto__;
var G__1790 = cljs.core.count.call(null,c__4351__auto__);
var G__1791 = (0);
seq__1762 = G__1788;
chunk__1763 = G__1789;
count__1764 = G__1790;
i__1765 = G__1791;
continue;
} else {
var vec__1769 = cljs.core.first.call(null,seq__1762__$1);
var effect_key = cljs.core.nth.call(null,vec__1769,(0),null);
var effect_value = cljs.core.nth.call(null,vec__1769,(1),null);
var temp__5455__auto___1792 = re_frame.registrar.get_handler.call(null,re_frame.fx.kind,effect_key,false);
if(cljs.core.truth_(temp__5455__auto___1792)){
var effect_fn_1793 = temp__5455__auto___1792;
effect_fn_1793.call(null,effect_value);
} else {
re_frame.loggers.console.call(null,new cljs.core.Keyword(null,"error","error",-978969032),"re-frame: no handler registered for effect:",effect_key,". Ignoring.");
}


var G__1794 = cljs.core.next.call(null,seq__1762__$1);
var G__1795 = null;
var G__1796 = (0);
var G__1797 = (0);
seq__1762 = G__1794;
chunk__1763 = G__1795;
count__1764 = G__1796;
i__1765 = G__1797;
continue;
}
} else {
return null;
}
}
break;
}
}finally {if(re_frame.trace.is_trace_enabled_QMARK_.call(null)){
var end__1506__auto___1798 = re_frame.interop.now.call(null);
var duration__1507__auto___1799 = (end__1506__auto___1798 - new cljs.core.Keyword(null,"start","start",-355208981).cljs$core$IFn$_invoke$arity$1(re_frame.trace._STAR_current_trace_STAR_));
cljs.core.swap_BANG_.call(null,re_frame.trace.traces,cljs.core.conj,cljs.core.assoc.call(null,re_frame.trace._STAR_current_trace_STAR_,new cljs.core.Keyword(null,"duration","duration",1444101068),duration__1507__auto___1799,new cljs.core.Keyword(null,"end","end",-268185958),re_frame.interop.now.call(null)));

re_frame.trace.run_tracing_callbacks_BANG_.call(null,end__1506__auto___1798);
} else {
}
}}finally {re_frame.trace._STAR_current_trace_STAR_ = _STAR_current_trace_STAR_1761;
}} else {
var seq__1772 = cljs.core.seq.call(null,new cljs.core.Keyword(null,"effects","effects",-282369292).cljs$core$IFn$_invoke$arity$1(context));
var chunk__1773 = null;
var count__1774 = (0);
var i__1775 = (0);
while(true){
if((i__1775 < count__1774)){
var vec__1776 = cljs.core._nth.call(null,chunk__1773,i__1775);
var effect_key = cljs.core.nth.call(null,vec__1776,(0),null);
var effect_value = cljs.core.nth.call(null,vec__1776,(1),null);
var temp__5455__auto___1800 = re_frame.registrar.get_handler.call(null,re_frame.fx.kind,effect_key,false);
if(cljs.core.truth_(temp__5455__auto___1800)){
var effect_fn_1801 = temp__5455__auto___1800;
effect_fn_1801.call(null,effect_value);
} else {
re_frame.loggers.console.call(null,new cljs.core.Keyword(null,"error","error",-978969032),"re-frame: no handler registered for effect:",effect_key,". Ignoring.");
}


var G__1802 = seq__1772;
var G__1803 = chunk__1773;
var G__1804 = count__1774;
var G__1805 = (i__1775 + (1));
seq__1772 = G__1802;
chunk__1773 = G__1803;
count__1774 = G__1804;
i__1775 = G__1805;
continue;
} else {
var temp__5457__auto__ = cljs.core.seq.call(null,seq__1772);
if(temp__5457__auto__){
var seq__1772__$1 = temp__5457__auto__;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__1772__$1)){
var c__4351__auto__ = cljs.core.chunk_first.call(null,seq__1772__$1);
var G__1806 = cljs.core.chunk_rest.call(null,seq__1772__$1);
var G__1807 = c__4351__auto__;
var G__1808 = cljs.core.count.call(null,c__4351__auto__);
var G__1809 = (0);
seq__1772 = G__1806;
chunk__1773 = G__1807;
count__1774 = G__1808;
i__1775 = G__1809;
continue;
} else {
var vec__1779 = cljs.core.first.call(null,seq__1772__$1);
var effect_key = cljs.core.nth.call(null,vec__1779,(0),null);
var effect_value = cljs.core.nth.call(null,vec__1779,(1),null);
var temp__5455__auto___1810 = re_frame.registrar.get_handler.call(null,re_frame.fx.kind,effect_key,false);
if(cljs.core.truth_(temp__5455__auto___1810)){
var effect_fn_1811 = temp__5455__auto___1810;
effect_fn_1811.call(null,effect_value);
} else {
re_frame.loggers.console.call(null,new cljs.core.Keyword(null,"error","error",-978969032),"re-frame: no handler registered for effect:",effect_key,". Ignoring.");
}


var G__1812 = cljs.core.next.call(null,seq__1772__$1);
var G__1813 = null;
var G__1814 = (0);
var G__1815 = (0);
seq__1772 = G__1812;
chunk__1773 = G__1813;
count__1774 = G__1814;
i__1775 = G__1815;
continue;
}
} else {
return null;
}
}
break;
}
}
}));
re_frame.fx.reg_fx.call(null,new cljs.core.Keyword(null,"dispatch-later","dispatch-later",291951390),(function (value){
var seq__1816 = cljs.core.seq.call(null,cljs.core.remove.call(null,cljs.core.nil_QMARK_,value));
var chunk__1817 = null;
var count__1818 = (0);
var i__1819 = (0);
while(true){
if((i__1819 < count__1818)){
var map__1820 = cljs.core._nth.call(null,chunk__1817,i__1819);
var map__1820__$1 = ((((!((map__1820 == null)))?(((((map__1820.cljs$lang$protocol_mask$partition0$ & (64))) || ((cljs.core.PROTOCOL_SENTINEL === map__1820.cljs$core$ISeq$))))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__1820):map__1820);
var effect = map__1820__$1;
var ms = cljs.core.get.call(null,map__1820__$1,new cljs.core.Keyword(null,"ms","ms",-1152709733));
var dispatch = cljs.core.get.call(null,map__1820__$1,new cljs.core.Keyword(null,"dispatch","dispatch",1319337009));
if(((cljs.core.empty_QMARK_.call(null,dispatch)) || (!(typeof ms === 'number')))){
re_frame.loggers.console.call(null,new cljs.core.Keyword(null,"error","error",-978969032),"re-frame: ignoring bad :dispatch-later value:",effect);
} else {
re_frame.interop.set_timeout_BANG_.call(null,((function (seq__1816,chunk__1817,count__1818,i__1819,map__1820,map__1820__$1,effect,ms,dispatch){
return (function (){
return re_frame.router.dispatch.call(null,dispatch);
});})(seq__1816,chunk__1817,count__1818,i__1819,map__1820,map__1820__$1,effect,ms,dispatch))
,ms);
}


var G__1824 = seq__1816;
var G__1825 = chunk__1817;
var G__1826 = count__1818;
var G__1827 = (i__1819 + (1));
seq__1816 = G__1824;
chunk__1817 = G__1825;
count__1818 = G__1826;
i__1819 = G__1827;
continue;
} else {
var temp__5457__auto__ = cljs.core.seq.call(null,seq__1816);
if(temp__5457__auto__){
var seq__1816__$1 = temp__5457__auto__;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__1816__$1)){
var c__4351__auto__ = cljs.core.chunk_first.call(null,seq__1816__$1);
var G__1828 = cljs.core.chunk_rest.call(null,seq__1816__$1);
var G__1829 = c__4351__auto__;
var G__1830 = cljs.core.count.call(null,c__4351__auto__);
var G__1831 = (0);
seq__1816 = G__1828;
chunk__1817 = G__1829;
count__1818 = G__1830;
i__1819 = G__1831;
continue;
} else {
var map__1822 = cljs.core.first.call(null,seq__1816__$1);
var map__1822__$1 = ((((!((map__1822 == null)))?(((((map__1822.cljs$lang$protocol_mask$partition0$ & (64))) || ((cljs.core.PROTOCOL_SENTINEL === map__1822.cljs$core$ISeq$))))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__1822):map__1822);
var effect = map__1822__$1;
var ms = cljs.core.get.call(null,map__1822__$1,new cljs.core.Keyword(null,"ms","ms",-1152709733));
var dispatch = cljs.core.get.call(null,map__1822__$1,new cljs.core.Keyword(null,"dispatch","dispatch",1319337009));
if(((cljs.core.empty_QMARK_.call(null,dispatch)) || (!(typeof ms === 'number')))){
re_frame.loggers.console.call(null,new cljs.core.Keyword(null,"error","error",-978969032),"re-frame: ignoring bad :dispatch-later value:",effect);
} else {
re_frame.interop.set_timeout_BANG_.call(null,((function (seq__1816,chunk__1817,count__1818,i__1819,map__1822,map__1822__$1,effect,ms,dispatch,seq__1816__$1,temp__5457__auto__){
return (function (){
return re_frame.router.dispatch.call(null,dispatch);
});})(seq__1816,chunk__1817,count__1818,i__1819,map__1822,map__1822__$1,effect,ms,dispatch,seq__1816__$1,temp__5457__auto__))
,ms);
}


var G__1832 = cljs.core.next.call(null,seq__1816__$1);
var G__1833 = null;
var G__1834 = (0);
var G__1835 = (0);
seq__1816 = G__1832;
chunk__1817 = G__1833;
count__1818 = G__1834;
i__1819 = G__1835;
continue;
}
} else {
return null;
}
}
break;
}
}));
re_frame.fx.reg_fx.call(null,new cljs.core.Keyword(null,"dispatch","dispatch",1319337009),(function (value){
if(!(cljs.core.vector_QMARK_.call(null,value))){
return re_frame.loggers.console.call(null,new cljs.core.Keyword(null,"error","error",-978969032),"re-frame: ignoring bad :dispatch value. Expected a vector, but got:",value);
} else {
return re_frame.router.dispatch.call(null,value);
}
}));
re_frame.fx.reg_fx.call(null,new cljs.core.Keyword(null,"dispatch-n","dispatch-n",-504469236),(function (value){
if(!(cljs.core.sequential_QMARK_.call(null,value))){
return re_frame.loggers.console.call(null,new cljs.core.Keyword(null,"error","error",-978969032),"re-frame: ignoring bad :dispatch-n value. Expected a collection, got got:",value);
} else {
var seq__1836 = cljs.core.seq.call(null,cljs.core.remove.call(null,cljs.core.nil_QMARK_,value));
var chunk__1837 = null;
var count__1838 = (0);
var i__1839 = (0);
while(true){
if((i__1839 < count__1838)){
var event = cljs.core._nth.call(null,chunk__1837,i__1839);
re_frame.router.dispatch.call(null,event);


var G__1840 = seq__1836;
var G__1841 = chunk__1837;
var G__1842 = count__1838;
var G__1843 = (i__1839 + (1));
seq__1836 = G__1840;
chunk__1837 = G__1841;
count__1838 = G__1842;
i__1839 = G__1843;
continue;
} else {
var temp__5457__auto__ = cljs.core.seq.call(null,seq__1836);
if(temp__5457__auto__){
var seq__1836__$1 = temp__5457__auto__;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__1836__$1)){
var c__4351__auto__ = cljs.core.chunk_first.call(null,seq__1836__$1);
var G__1844 = cljs.core.chunk_rest.call(null,seq__1836__$1);
var G__1845 = c__4351__auto__;
var G__1846 = cljs.core.count.call(null,c__4351__auto__);
var G__1847 = (0);
seq__1836 = G__1844;
chunk__1837 = G__1845;
count__1838 = G__1846;
i__1839 = G__1847;
continue;
} else {
var event = cljs.core.first.call(null,seq__1836__$1);
re_frame.router.dispatch.call(null,event);


var G__1848 = cljs.core.next.call(null,seq__1836__$1);
var G__1849 = null;
var G__1850 = (0);
var G__1851 = (0);
seq__1836 = G__1848;
chunk__1837 = G__1849;
count__1838 = G__1850;
i__1839 = G__1851;
continue;
}
} else {
return null;
}
}
break;
}
}
}));
re_frame.fx.reg_fx.call(null,new cljs.core.Keyword(null,"deregister-event-handler","deregister-event-handler",-1096518994),(function (value){
var clear_event = cljs.core.partial.call(null,re_frame.registrar.clear_handlers,re_frame.events.kind);
if(cljs.core.sequential_QMARK_.call(null,value)){
var seq__1852 = cljs.core.seq.call(null,value);
var chunk__1853 = null;
var count__1854 = (0);
var i__1855 = (0);
while(true){
if((i__1855 < count__1854)){
var event = cljs.core._nth.call(null,chunk__1853,i__1855);
clear_event.call(null,event);


var G__1856 = seq__1852;
var G__1857 = chunk__1853;
var G__1858 = count__1854;
var G__1859 = (i__1855 + (1));
seq__1852 = G__1856;
chunk__1853 = G__1857;
count__1854 = G__1858;
i__1855 = G__1859;
continue;
} else {
var temp__5457__auto__ = cljs.core.seq.call(null,seq__1852);
if(temp__5457__auto__){
var seq__1852__$1 = temp__5457__auto__;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__1852__$1)){
var c__4351__auto__ = cljs.core.chunk_first.call(null,seq__1852__$1);
var G__1860 = cljs.core.chunk_rest.call(null,seq__1852__$1);
var G__1861 = c__4351__auto__;
var G__1862 = cljs.core.count.call(null,c__4351__auto__);
var G__1863 = (0);
seq__1852 = G__1860;
chunk__1853 = G__1861;
count__1854 = G__1862;
i__1855 = G__1863;
continue;
} else {
var event = cljs.core.first.call(null,seq__1852__$1);
clear_event.call(null,event);


var G__1864 = cljs.core.next.call(null,seq__1852__$1);
var G__1865 = null;
var G__1866 = (0);
var G__1867 = (0);
seq__1852 = G__1864;
chunk__1853 = G__1865;
count__1854 = G__1866;
i__1855 = G__1867;
continue;
}
} else {
return null;
}
}
break;
}
} else {
return clear_event.call(null,value);
}
}));
re_frame.fx.reg_fx.call(null,new cljs.core.Keyword(null,"db","db",993250759),(function (value){
if(!((cljs.core.deref.call(null,re_frame.db.app_db) === value))){
return cljs.core.reset_BANG_.call(null,re_frame.db.app_db,value);
} else {
return null;
}
}));

//# sourceMappingURL=fx.js.map
