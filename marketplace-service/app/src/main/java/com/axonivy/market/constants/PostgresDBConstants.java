package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * <p>
 * PostgreSQL database constants defining column/field names and SQL ordering keywords for database queries and
 * entity mapping.
 * </p>
 *
 * @since 15/04/2026
 * @author tvtphuc
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostgresDBConstants {
  public static final String SYNCHRONIZED_INSTALLATION_COUNT = "synchronizedInstallationCount";
  public static final String DESIGNER_VERSION = "designerVersion";
  public static final String VERSION = "version";
  public static final String DOC = "doc";
  public static final String WORKFLOW_TYPE = "workflowType";
  public static final String ASCENDING = "ASC";
  public static final String DESCENDING = "DESC";
}
