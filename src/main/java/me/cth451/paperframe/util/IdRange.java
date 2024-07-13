package me.cth451.paperframe.util;

import java.util.Collection;
import java.util.LinkedList;

public class IdRange {
	/**
	 * Parse ID range according to command specs
	 *
	 * @param id_spec one id range string
	 * @return expanded IDs
	 * @throws NumberFormatException if input is malformed
	 */
	static public LinkedList<Integer> parseIdRange(String id_spec) throws NumberFormatException {
		LinkedList<Integer> ids = new LinkedList<>();
		try {
			int colon_sep_idx = id_spec.indexOf(':');
			int plus_sep_idx = id_spec.indexOf('+');
			if (colon_sep_idx != -1) {
				int start = Integer.parseUnsignedInt(id_spec.substring(0, colon_sep_idx));
				int end = Integer.parseUnsignedInt(id_spec.substring(colon_sep_idx + 1));
				int step = start <= end ? 1 : -1;
				for (int i = start; step * (i - end) <= 0; i += step) {
					ids.add(i);
				}
			} else if (plus_sep_idx != -1) {
				int base = Integer.parseUnsignedInt(id_spec.substring(0, plus_sep_idx));
				/* This could be signed */
				int offset = Integer.parseInt(id_spec.substring(plus_sep_idx + 1));
				/* Negative = backwards */
				int step = offset >= 0 ? 1 : -1;
				/* This is always positive */
				int start = step > 0 ? base : base + offset * step;
				int end = step > 0 ? base + offset * step : base;

				for (int i = start; step * (i - end) <= 0; i += step) {
					ids.add(i);
				}
			} else {
				ids.add(Integer.parseUnsignedInt(id_spec));
			}
		} catch (NumberFormatException e) {
			throw new NumberFormatException(String.format("%s is not a valid map ID range", id_spec));
		}
		return ids;
	}

	/**
	 * Parse ID ranges according to command specs
	 *
	 * @param id_specs a list of map ID ranges
	 * @return expanded IDs
	 * @throws NumberFormatException if input is malformed
	 */
	static public LinkedList<Integer> parseIdRanges(Collection<String> id_specs) throws NumberFormatException {
		LinkedList<Integer> ids = new LinkedList<>();
		for (String id_spec : id_specs) {
			ids.addAll(parseIdRange(id_spec));
		}
		return ids;
	}
}
