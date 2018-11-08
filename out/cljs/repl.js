// Compiled by ClojureScript 1.10.339 {}
goog.provide('cljs.repl');
goog.require('cljs.core');
goog.require('cljs.spec.alpha');
cljs.repl.print_doc = (function cljs$repl$print_doc(p__1428){
var map__1429 = p__1428;
var map__1429__$1 = ((((!((map__1429 == null)))?(((((map__1429.cljs$lang$protocol_mask$partition0$ & (64))) || ((cljs.core.PROTOCOL_SENTINEL === map__1429.cljs$core$ISeq$))))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__1429):map__1429);
var m = map__1429__$1;
var n = cljs.core.get.call(null,map__1429__$1,new cljs.core.Keyword(null,"ns","ns",441598760));
var nm = cljs.core.get.call(null,map__1429__$1,new cljs.core.Keyword(null,"name","name",1843675177));
cljs.core.println.call(null,"-------------------------");

cljs.core.println.call(null,[cljs.core.str.cljs$core$IFn$_invoke$arity$1((function (){var temp__5457__auto__ = new cljs.core.Keyword(null,"ns","ns",441598760).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_(temp__5457__auto__)){
var ns = temp__5457__auto__;
return [cljs.core.str.cljs$core$IFn$_invoke$arity$1(ns),"/"].join('');
} else {
return null;
}
})()),cljs.core.str.cljs$core$IFn$_invoke$arity$1(new cljs.core.Keyword(null,"name","name",1843675177).cljs$core$IFn$_invoke$arity$1(m))].join(''));

if(cljs.core.truth_(new cljs.core.Keyword(null,"protocol","protocol",652470118).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Protocol");
} else {
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"forms","forms",2045992350).cljs$core$IFn$_invoke$arity$1(m))){
var seq__1431_1453 = cljs.core.seq.call(null,new cljs.core.Keyword(null,"forms","forms",2045992350).cljs$core$IFn$_invoke$arity$1(m));
var chunk__1432_1454 = null;
var count__1433_1455 = (0);
var i__1434_1456 = (0);
while(true){
if((i__1434_1456 < count__1433_1455)){
var f_1457 = cljs.core._nth.call(null,chunk__1432_1454,i__1434_1456);
cljs.core.println.call(null,"  ",f_1457);


var G__1458 = seq__1431_1453;
var G__1459 = chunk__1432_1454;
var G__1460 = count__1433_1455;
var G__1461 = (i__1434_1456 + (1));
seq__1431_1453 = G__1458;
chunk__1432_1454 = G__1459;
count__1433_1455 = G__1460;
i__1434_1456 = G__1461;
continue;
} else {
var temp__5457__auto___1462 = cljs.core.seq.call(null,seq__1431_1453);
if(temp__5457__auto___1462){
var seq__1431_1463__$1 = temp__5457__auto___1462;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__1431_1463__$1)){
var c__4351__auto___1464 = cljs.core.chunk_first.call(null,seq__1431_1463__$1);
var G__1465 = cljs.core.chunk_rest.call(null,seq__1431_1463__$1);
var G__1466 = c__4351__auto___1464;
var G__1467 = cljs.core.count.call(null,c__4351__auto___1464);
var G__1468 = (0);
seq__1431_1453 = G__1465;
chunk__1432_1454 = G__1466;
count__1433_1455 = G__1467;
i__1434_1456 = G__1468;
continue;
} else {
var f_1469 = cljs.core.first.call(null,seq__1431_1463__$1);
cljs.core.println.call(null,"  ",f_1469);


var G__1470 = cljs.core.next.call(null,seq__1431_1463__$1);
var G__1471 = null;
var G__1472 = (0);
var G__1473 = (0);
seq__1431_1453 = G__1470;
chunk__1432_1454 = G__1471;
count__1433_1455 = G__1472;
i__1434_1456 = G__1473;
continue;
}
} else {
}
}
break;
}
} else {
if(cljs.core.truth_(new cljs.core.Keyword(null,"arglists","arglists",1661989754).cljs$core$IFn$_invoke$arity$1(m))){
var arglists_1474 = new cljs.core.Keyword(null,"arglists","arglists",1661989754).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_((function (){var or__3949__auto__ = new cljs.core.Keyword(null,"macro","macro",-867863404).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_(or__3949__auto__)){
return or__3949__auto__;
} else {
return new cljs.core.Keyword(null,"repl-special-function","repl-special-function",1262603725).cljs$core$IFn$_invoke$arity$1(m);
}
})())){
cljs.core.prn.call(null,arglists_1474);
} else {
cljs.core.prn.call(null,((cljs.core._EQ_.call(null,new cljs.core.Symbol(null,"quote","quote",1377916282,null),cljs.core.first.call(null,arglists_1474)))?cljs.core.second.call(null,arglists_1474):arglists_1474));
}
} else {
}
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"special-form","special-form",-1326536374).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Special Form");

cljs.core.println.call(null," ",new cljs.core.Keyword(null,"doc","doc",1913296891).cljs$core$IFn$_invoke$arity$1(m));

if(cljs.core.contains_QMARK_.call(null,m,new cljs.core.Keyword(null,"url","url",276297046))){
if(cljs.core.truth_(new cljs.core.Keyword(null,"url","url",276297046).cljs$core$IFn$_invoke$arity$1(m))){
return cljs.core.println.call(null,["\n  Please see http://clojure.org/",cljs.core.str.cljs$core$IFn$_invoke$arity$1(new cljs.core.Keyword(null,"url","url",276297046).cljs$core$IFn$_invoke$arity$1(m))].join(''));
} else {
return null;
}
} else {
return cljs.core.println.call(null,["\n  Please see http://clojure.org/special_forms#",cljs.core.str.cljs$core$IFn$_invoke$arity$1(new cljs.core.Keyword(null,"name","name",1843675177).cljs$core$IFn$_invoke$arity$1(m))].join(''));
}
} else {
if(cljs.core.truth_(new cljs.core.Keyword(null,"macro","macro",-867863404).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Macro");
} else {
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"repl-special-function","repl-special-function",1262603725).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"REPL Special Function");
} else {
}

cljs.core.println.call(null," ",new cljs.core.Keyword(null,"doc","doc",1913296891).cljs$core$IFn$_invoke$arity$1(m));

if(cljs.core.truth_(new cljs.core.Keyword(null,"protocol","protocol",652470118).cljs$core$IFn$_invoke$arity$1(m))){
var seq__1435_1475 = cljs.core.seq.call(null,new cljs.core.Keyword(null,"methods","methods",453930866).cljs$core$IFn$_invoke$arity$1(m));
var chunk__1436_1476 = null;
var count__1437_1477 = (0);
var i__1438_1478 = (0);
while(true){
if((i__1438_1478 < count__1437_1477)){
var vec__1439_1479 = cljs.core._nth.call(null,chunk__1436_1476,i__1438_1478);
var name_1480 = cljs.core.nth.call(null,vec__1439_1479,(0),null);
var map__1442_1481 = cljs.core.nth.call(null,vec__1439_1479,(1),null);
var map__1442_1482__$1 = ((((!((map__1442_1481 == null)))?(((((map__1442_1481.cljs$lang$protocol_mask$partition0$ & (64))) || ((cljs.core.PROTOCOL_SENTINEL === map__1442_1481.cljs$core$ISeq$))))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__1442_1481):map__1442_1481);
var doc_1483 = cljs.core.get.call(null,map__1442_1482__$1,new cljs.core.Keyword(null,"doc","doc",1913296891));
var arglists_1484 = cljs.core.get.call(null,map__1442_1482__$1,new cljs.core.Keyword(null,"arglists","arglists",1661989754));
cljs.core.println.call(null);

cljs.core.println.call(null," ",name_1480);

cljs.core.println.call(null," ",arglists_1484);

if(cljs.core.truth_(doc_1483)){
cljs.core.println.call(null," ",doc_1483);
} else {
}


var G__1485 = seq__1435_1475;
var G__1486 = chunk__1436_1476;
var G__1487 = count__1437_1477;
var G__1488 = (i__1438_1478 + (1));
seq__1435_1475 = G__1485;
chunk__1436_1476 = G__1486;
count__1437_1477 = G__1487;
i__1438_1478 = G__1488;
continue;
} else {
var temp__5457__auto___1489 = cljs.core.seq.call(null,seq__1435_1475);
if(temp__5457__auto___1489){
var seq__1435_1490__$1 = temp__5457__auto___1489;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__1435_1490__$1)){
var c__4351__auto___1491 = cljs.core.chunk_first.call(null,seq__1435_1490__$1);
var G__1492 = cljs.core.chunk_rest.call(null,seq__1435_1490__$1);
var G__1493 = c__4351__auto___1491;
var G__1494 = cljs.core.count.call(null,c__4351__auto___1491);
var G__1495 = (0);
seq__1435_1475 = G__1492;
chunk__1436_1476 = G__1493;
count__1437_1477 = G__1494;
i__1438_1478 = G__1495;
continue;
} else {
var vec__1444_1496 = cljs.core.first.call(null,seq__1435_1490__$1);
var name_1497 = cljs.core.nth.call(null,vec__1444_1496,(0),null);
var map__1447_1498 = cljs.core.nth.call(null,vec__1444_1496,(1),null);
var map__1447_1499__$1 = ((((!((map__1447_1498 == null)))?(((((map__1447_1498.cljs$lang$protocol_mask$partition0$ & (64))) || ((cljs.core.PROTOCOL_SENTINEL === map__1447_1498.cljs$core$ISeq$))))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__1447_1498):map__1447_1498);
var doc_1500 = cljs.core.get.call(null,map__1447_1499__$1,new cljs.core.Keyword(null,"doc","doc",1913296891));
var arglists_1501 = cljs.core.get.call(null,map__1447_1499__$1,new cljs.core.Keyword(null,"arglists","arglists",1661989754));
cljs.core.println.call(null);

cljs.core.println.call(null," ",name_1497);

cljs.core.println.call(null," ",arglists_1501);

if(cljs.core.truth_(doc_1500)){
cljs.core.println.call(null," ",doc_1500);
} else {
}


var G__1502 = cljs.core.next.call(null,seq__1435_1490__$1);
var G__1503 = null;
var G__1504 = (0);
var G__1505 = (0);
seq__1435_1475 = G__1502;
chunk__1436_1476 = G__1503;
count__1437_1477 = G__1504;
i__1438_1478 = G__1505;
continue;
}
} else {
}
}
break;
}
} else {
}

if(cljs.core.truth_(n)){
var temp__5457__auto__ = cljs.spec.alpha.get_spec.call(null,cljs.core.symbol.call(null,[cljs.core.str.cljs$core$IFn$_invoke$arity$1(cljs.core.ns_name.call(null,n))].join(''),cljs.core.name.call(null,nm)));
if(cljs.core.truth_(temp__5457__auto__)){
var fnspec = temp__5457__auto__;
cljs.core.print.call(null,"Spec");

var seq__1449 = cljs.core.seq.call(null,new cljs.core.PersistentVector(null, 3, 5, cljs.core.PersistentVector.EMPTY_NODE, [new cljs.core.Keyword(null,"args","args",1315556576),new cljs.core.Keyword(null,"ret","ret",-468222814),new cljs.core.Keyword(null,"fn","fn",-1175266204)], null));
var chunk__1450 = null;
var count__1451 = (0);
var i__1452 = (0);
while(true){
if((i__1452 < count__1451)){
var role = cljs.core._nth.call(null,chunk__1450,i__1452);
var temp__5457__auto___1506__$1 = cljs.core.get.call(null,fnspec,role);
if(cljs.core.truth_(temp__5457__auto___1506__$1)){
var spec_1507 = temp__5457__auto___1506__$1;
cljs.core.print.call(null,["\n ",cljs.core.str.cljs$core$IFn$_invoke$arity$1(cljs.core.name.call(null,role)),":"].join(''),cljs.spec.alpha.describe.call(null,spec_1507));
} else {
}


var G__1508 = seq__1449;
var G__1509 = chunk__1450;
var G__1510 = count__1451;
var G__1511 = (i__1452 + (1));
seq__1449 = G__1508;
chunk__1450 = G__1509;
count__1451 = G__1510;
i__1452 = G__1511;
continue;
} else {
var temp__5457__auto____$1 = cljs.core.seq.call(null,seq__1449);
if(temp__5457__auto____$1){
var seq__1449__$1 = temp__5457__auto____$1;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__1449__$1)){
var c__4351__auto__ = cljs.core.chunk_first.call(null,seq__1449__$1);
var G__1512 = cljs.core.chunk_rest.call(null,seq__1449__$1);
var G__1513 = c__4351__auto__;
var G__1514 = cljs.core.count.call(null,c__4351__auto__);
var G__1515 = (0);
seq__1449 = G__1512;
chunk__1450 = G__1513;
count__1451 = G__1514;
i__1452 = G__1515;
continue;
} else {
var role = cljs.core.first.call(null,seq__1449__$1);
var temp__5457__auto___1516__$2 = cljs.core.get.call(null,fnspec,role);
if(cljs.core.truth_(temp__5457__auto___1516__$2)){
var spec_1517 = temp__5457__auto___1516__$2;
cljs.core.print.call(null,["\n ",cljs.core.str.cljs$core$IFn$_invoke$arity$1(cljs.core.name.call(null,role)),":"].join(''),cljs.spec.alpha.describe.call(null,spec_1517));
} else {
}


var G__1518 = cljs.core.next.call(null,seq__1449__$1);
var G__1519 = null;
var G__1520 = (0);
var G__1521 = (0);
seq__1449 = G__1518;
chunk__1450 = G__1519;
count__1451 = G__1520;
i__1452 = G__1521;
continue;
}
} else {
return null;
}
}
break;
}
} else {
return null;
}
} else {
return null;
}
}
});

//# sourceMappingURL=repl.js.map
