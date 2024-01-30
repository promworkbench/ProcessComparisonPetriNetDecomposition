package org.processmining.processcomparisonpetrinetdecomposition.util;

import java.time.Instant;
import java.util.Date;

import org.apache.commons.lang3.tuple.Pair;
import org.deckfour.xes.model.XLog;
import org.processmining.log.utils.XLogBuilder;

public class TestLogProvider {
	static int timedelta_seconds = 100;
	static int timedelta_seconds_exception = 5000;
	
	/**
	 * Build two artificial logs with conditional choices.
	 * @return
	 */
	public static Pair<XLog, XLog> createArtificalLogsConditional() {
		/*
		 * Left Log:
		 * A->B very frequent: 50% : 50% for choice
		 * <A, B, C>^{35}
		 * <A, B, D>^{35}
		 *
		 * A->E less frequent: 50% : 50% for choice
		 * <A, E, F>^{15}
		 * <A, E, G>^{15}
		 *
		 * Right Log:
		 * A->B less frequent: 50% : 50% for choice
		 * <A, B, C>^{25}
		 * <A, B, D>^{25}
		 *
		 * A->E more frequent: 33% : 66% for choice
		 * <A, E, F>^{15}
		 * <A, E, G>^{35}
		 */
		// Build logs
		// Left log
		int tracesL = 0;
		XLogBuilder logBuilderL = XLogBuilder.newInstance().startLog("Log left");
		for (int i = 0; i < 10; i++) {
			Date timestamp = Date.from(Instant.EPOCH.plusSeconds(i).plusSeconds(1000000*(i/2)));
			logBuilderL.addTrace("L " + tracesL);
			logBuilderL.addEvent("T1");
			logBuilderL.addAttribute("time:timestamp", timestamp);
			timestamp = Date.from(timestamp.toInstant().plusSeconds(timedelta_seconds_exception));
			logBuilderL.addEvent("T2");
			logBuilderL.addAttribute("time:timestamp", timestamp);
			timestamp = Date.from(timestamp.toInstant().plusSeconds(timedelta_seconds));
			logBuilderL.addEvent("T3");
			logBuilderL.addAttribute("time:timestamp", timestamp);
			timestamp = Date.from(timestamp.toInstant().plusSeconds(timedelta_seconds));
//			logBuilderL.addEvent("T4");
//			logBuilderL.addAttribute("time:timestamp", timestamp);
//			timestamp = Date.from(timestamp.toInstant().plusSeconds(timedelta_seconds));
			logBuilderL.addEvent("T5");
			logBuilderL.addAttribute("time:timestamp", timestamp);
			tracesL++;
		}
//		for (int i = 0; i < 2; i++) {
//			Date timestamp = Date.from(Instant.EPOCH);
//			logBuilderL.addTrace("L " + tracesL);
//			logBuilderL.addEvent("T1");
//			logBuilderL.addAttribute("time:timestamp", timestamp);
//			timestamp = Date.from(timestamp.toInstant().plusSeconds(timedelta_seconds));
////			logBuilderL.addEvent("T2");
////			logBuilderL.addAttribute("time:timestamp", timestamp);
////			timestamp = Date.from(timestamp.toInstant().plusSeconds(timedelta_seconds));
//			logBuilderL.addEvent("T3");
//			logBuilderL.addAttribute("time:timestamp", timestamp);
//			timestamp = Date.from(timestamp.toInstant().plusSeconds(timedelta_seconds));
//			logBuilderL.addEvent("T5");
//			logBuilderL.addAttribute("time:timestamp", timestamp);
//			tracesL++;
//		}
//		for (int i = 0; i < 15; i++) {
//			logBuilderL.addTrace("L " + tracesL)
//					.addEvent("A").addEvent("E").addEvent("F");
//			tracesL++;
//		}
//		for (int i = 0; i < 15; i++) {
//			logBuilderL.addTrace("L " + tracesL)
//					.addEvent("A").addEvent("E").addEvent("G");
//			tracesL++;
//		}
		// Right Log
		int tracesR = 0;
		XLogBuilder logBuilderR = XLogBuilder.newInstance().startLog("Log right");
		for (int i = 0; i < 10; i++) {
			Date timestamp = Date.from(Instant.EPOCH);
			logBuilderR.addTrace("L " + tracesR);
			logBuilderR.addEvent("T1");
			logBuilderR.addAttribute("time:timestamp", timestamp);
			timestamp = Date.from(timestamp.toInstant().plusSeconds(timedelta_seconds));
//			logBuilderR.addEvent("T3");
//			logBuilderR.addAttribute("time:timestamp", timestamp);
//			timestamp = Date.from(timestamp.toInstant().plusSeconds(timedelta_seconds));
			logBuilderR.addEvent("T4");
			logBuilderR.addAttribute("time:timestamp", timestamp);
			timestamp = Date.from(timestamp.toInstant().plusSeconds(timedelta_seconds));
			logBuilderR.addEvent("T2");
			logBuilderR.addAttribute("time:timestamp", timestamp);
			timestamp = Date.from(timestamp.toInstant().plusSeconds(timedelta_seconds));
			logBuilderR.addEvent("T5");
			logBuilderR.addAttribute("time:timestamp", timestamp);
			tracesR++;
		}
		for (int i = 0; i < 2; i++) {
			Date timestamp = Date.from(Instant.EPOCH);
			logBuilderR.addTrace("L " + tracesR);
			logBuilderR.addEvent("T1");
			logBuilderR.addAttribute("time:timestamp", timestamp);
			timestamp = Date.from(timestamp.toInstant().plusSeconds(timedelta_seconds));
			logBuilderR.addEvent("T4");
			logBuilderR.addAttribute("time:timestamp", timestamp);
			timestamp = Date.from(timestamp.toInstant().plusSeconds(timedelta_seconds));
			logBuilderR.addEvent("T2");
			logBuilderR.addAttribute("time:timestamp", timestamp);
			timestamp = Date.from(timestamp.toInstant().plusSeconds(timedelta_seconds));
			logBuilderR.addEvent("T5");
			logBuilderR.addAttribute("time:timestamp", timestamp);
			tracesR++;
		}
//		for (int i = 0; i < 15; i++) {
//			logBuilderR.addTrace("R " + tracesR)
//					.addEvent("A").addEvent("E").addEvent("F");
//			tracesR++;
//		}
//		for (int i = 0; i < 35; i++) {
//			logBuilderR.addTrace("R " + tracesR)
//					.addEvent("A").addEvent("E").addEvent("G");
//			tracesR++;
//		}
		XLog logL = logBuilderL.build();
		XLog logR = logBuilderR.build();
		// Add Lifecyles
//		for (XTrace t : logL) {
//			for (XEvent e : t) {
//				XLifecycleExtension.instance().assignStandardTransition(e, XLifecycleExtension.StandardModel.COMPLETE);
//			}
//		}
//		for (XTrace t : logR) {
//			for (XEvent e : t) {
//				XLifecycleExtension.instance().assignStandardTransition(e, XLifecycleExtension.StandardModel.COMPLETE);
//			}
//		}
		return Pair.of(logL, logR);
	}
}
