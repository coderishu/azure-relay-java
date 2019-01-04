package com.microsoft.azure.relay;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;

class TimeoutHelper {
	private LocalDateTime deadline;
	private boolean deadlineSet;
	private Duration originalTimeout;

	protected TimeoutHelper(Duration timeout) {
		this(timeout, false);
	}

	protected TimeoutHelper(Duration timeout, boolean startTimeout) {
		this.originalTimeout = timeout;
		this.deadline = LocalDateTime.MAX;
		this.deadlineSet = (timeout != null && isMaxDuration(timeout));

		if (startTimeout && !this.deadlineSet) {
			this.setDeadline();
		}
	}

	protected Duration getOriginalTimeout() {
		return this.originalTimeout;
	}

	protected static Duration fromMillis(int milliseconds) {
		if (milliseconds >= Integer.MAX_VALUE) {
			return RelayConstants.MAX_DURATION;
		} else {
			return Duration.ofMillis(milliseconds);
		}
	}

	protected static int toMillis(Duration timeout) {
		long millis = timeout.toMillis();
		return millis > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) millis;
	}

	protected static Duration min(Duration val1, Duration val2) {
		return (val1.compareTo(val2) < 0) ? val1 : val2;
	}

	protected static LocalDateTime min(LocalDateTime val1, LocalDateTime val2) {
		return (val1.compareTo(val2) < 0) ? val1 : val2;
	}

	protected static LocalDateTime add(LocalDateTime time, Duration timeout) {
		LocalDateTime temp = LocalDateTime.of(time.toLocalDate(), time.toLocalTime());
		try {
			temp = temp.plusNanos(timeout.toNanos());
		}
		// Catch if the result is out of bounds
		catch (DateTimeException e) {
			if (timeout.compareTo(Duration.ZERO) < 0) {
				return LocalDateTime.MIN;
			} else {
				return LocalDateTime.MAX;
			}
		}
		return temp;
	}

	protected static LocalDateTime subtract(LocalDateTime time, Duration timeout) {
		LocalDateTime temp = LocalDateTime.of(time.toLocalDate(), time.toLocalTime());
		try {
			temp = temp.minusNanos(timeout.toNanos());
		}
		// Catch if the result is out of bounds
		catch (DateTimeException e) {
			if (timeout.compareTo(Duration.ZERO) < 0) {
				return LocalDateTime.MIN;
			} else {
				return LocalDateTime.MAX;
			}
		}
		return temp;
	}

	protected Duration remainingTime() {
		if (!this.deadlineSet) {
			this.setDeadline();
			return this.originalTimeout;
		} else if (this.deadline == LocalDateTime.MAX) {
			return RelayConstants.MAX_DURATION;
		} else {
			Duration remaining = Duration.between(LocalDateTime.now(), this.deadline);
			return (remaining.compareTo(Duration.ZERO) < 0) ? Duration.ZERO : remaining;
		}
	}

	protected Duration elapsedTime() {
		return this.originalTimeout.minus(this.remainingTime());
	}

	private void setDeadline() {
		if (!deadlineSet) {
			this.deadline = add(LocalDateTime.now(), this.originalTimeout);
			this.deadlineSet = true;
		}
	}

	protected static void throwIfNegativeArgument(Duration timeout) {
		throwIfNegativeArgument(timeout, "timeout");
	}

	protected static void throwIfNegativeArgument(Duration timeout, String argumentName) {
		if (timeout != null && timeout.isNegative()) {
			throw new IllegalArgumentException("timeout interval cannot be negative.");
			// TODO: trace
//            throw RelayEventSource.Log.ArgumentOutOfRange(argumentName, timeout, SR.GetString(SR.TimeoutMustBeNonNegative, argumentName, timeout));
		}
	}

	private boolean isMaxDuration(Duration duration) {
		return duration.compareTo(RelayConstants.MAX_DURATION) >= 0
				|| duration.compareTo(RelayConstants.MIN_DURATION) <= 0;
	}
}
