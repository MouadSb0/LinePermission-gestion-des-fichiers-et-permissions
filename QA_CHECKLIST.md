# AuditDB Manual QA Checklist

## Startup

1. Compile the application with the SQLite and BCrypt JARs on the classpath.
2. Run `Main` with the `src/main/resources` directory on the classpath.
3. Confirm the application displays `Bienvenue dans LinePermission !`.
4. Confirm a local `lineperm.db` file is created.

## Authentication

1. Run `signup qa_user StrongPassword1`.
2. Confirm the success message is displayed.
3. Run the same signup command again.
4. Confirm a friendly duplicate-login message is displayed, with no stack trace.
5. Run `login qa_user StrongPassword1`.
6. Confirm the welcome message and authenticated prompt are displayed.
7. Run `login qa_user WrongPassword`.
8. Confirm the generic invalid-login message is displayed.
9. Run `logout`.
10. Confirm the logout message is displayed.

## Invalid input and resilience

1. Enter an unknown command such as `abc`.
2. Confirm the application remains running and displays an explanatory message.
3. Enter `stats refused-user abc`.
4. Confirm the invalid numeric identifier message is displayed.
5. Enter `stats` without a statistic name.
6. Confirm usage instructions are displayed.
7. Send end-of-file input (`Ctrl+Z`, then Enter on Windows).
8. Confirm the application exits cleanly.

## Audit statistics

After creating audit records through the application or test fixtures:

1. Log in and run `stats total`; verify the total action count.
2. Run `stats refused`; verify refused accesses.
3. Run `stats users`; verify distinct users.
4. Run `stats per-user`; verify each user ID and count.
5. Run `stats top-files`; verify no more than three file IDs are displayed.
6. Run `stats refused-user <id>`; verify the selected user's refused count.
7. Run `stats most-active`; verify one user ID or the empty-activity message.
8. Run `stats by-action`; verify each action type and count.
9. Confirm output is readable and contains no SQL stack traces.

## Database connection failure

1. Stop the application.
2. Set `DB_URL` to an invalid JDBC URL, for example:
   `set DB_URL=jdbc:sqlite:X:\does-not-exist\lineperm.db`
3. Start the application.
4. Confirm it displays a friendly database-unavailable message.
5. Restore or clear `DB_URL` before the next run.

SQLite normally allows concurrent readers, so a locked database may not fail
at startup. To test an unavailable database deterministically, use an invalid
JDBC URL or make the database path inaccessible to the current user.
