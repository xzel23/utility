package com.dua3.utility.lang;

import java.time.Duration;
import java.time.Instant;

/**
 * A simple stopwatch class.
 */
public class Stopwatch {

	private final String name;
	private final Instant start;
	
	/**
	 * Construct instance.
	 * @param name the name for this instance; it is included in {@code toString()} 
	 */
	public Stopwatch(String name) {
		this.name = name;
		this.start = Instant.now();
	}
	
	/**
	 * Get start instant.
	 * @return the instant when this stopwatch was created
	 */
	public Instant getStart() {
		return start;
	}

	/**
	 * Get elapsed time.
	 * @return the duration since this instance was created
	 */
	public Duration elapsed() {
		Instant now = Instant.now();
		return Duration.between(start, now);
	}
	
	@Override
	public String toString() {
		return "["+name+"] running for "+elapsedString();
	}

	private static String formatDuration(Duration duration) {
		boolean negative = duration.isNegative();
		
	    long seconds = duration.getSeconds();
	    long absSeconds = Math.abs(seconds);
	    int nano = Math.abs(duration.getNano());

	    long hr = absSeconds / 3600;
	    long min = (absSeconds % 3600) / 60;
	    double sec = absSeconds+nano/1_000_000_000.0;
		
	    String positive = String.format(
	        "%d:%02d:%06.3f",
	        hr,
	        min,
	        sec);
	    
	    return negative ? "-" + positive : positive;
	}
	
	/**
	 * Get elapsed time as a string.
	 * @return string with elapsed time 
	 */
	public String elapsedString() {
		return formatDuration(elapsed());
	}
}
