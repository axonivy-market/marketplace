package com.axonivy.market.exceptions;


import java.io.Serial;

import static com.axonivy.market.constants.ErrorMessageConstants.INVALID_MISSING_HEADER_ERROR_MESSAGE;

public class MissingHeaderException extends Exception {

	@Serial
    private static final long serialVersionUID = -793606549500983953L;

	public MissingHeaderException() {
        super(INVALID_MISSING_HEADER_ERROR_MESSAGE);
    }
}
