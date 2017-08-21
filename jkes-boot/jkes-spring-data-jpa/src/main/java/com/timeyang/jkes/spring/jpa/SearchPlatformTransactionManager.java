package com.timeyang.jkes.spring.jpa;

import com.timeyang.jkes.core.util.ReflectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.*;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronization;

import javax.transaction.RollbackException;
import javax.transaction.UserTransaction;
import java.util.List;

/**
 * SearchPlatformTransactionManager
 *
 * <p>Wrap any AbstractPlatformTransactionManager subclass, and hook into transaction intercept logic</p>
 *
 * <p>Wrap AbstractPlatformTransactionManager rather than PlatformTransactionManager directly. Because we need AbstractPlatformTransactionManager's protected method as API for us to hook into transaction intercept logic</p>
 *
 * <p>Because AbstractPlatformTransactionManager's subclasses can override all not final public and protected method in superclass, so we must override all these methods, and delegate to platformTransactionManager via reflection</p>
 *
 * <p>This is OK, because AbstractPlatformTransactionManager protected method is API to SearchPlatformTransactionManager</p>
 *
 * <p>You can't directly delegate EntityManager by wrapping EntityManagerImpl to intercept transaction. because transaction is in EntityTransaction. But you can intercept persist、merge and remove method, of course you can do that by EntityListener, @PrePersist.. .
 * <i>https://stackoverflow.com/questions/25553048/how-can-i-intercept-jta-transactions-events-and-get-a-reference-to-the-current-e</i>
 * </p>
 *
 * <p>combine delegating EntityManager and AbstractPlatformTransactionManager is a good choice in spring application for not using spring data jpa</p>
 * @author chaokunyang
 */
public class SearchPlatformTransactionManager extends AbstractPlatformTransactionManager {

    private static final Log LOGGER = LogFactory.getLog(SearchPlatformTransactionManager.class);

    private AbstractPlatformTransactionManager platformTransactionManager;

    private EventSupport eventSupport;

    public SearchPlatformTransactionManager(AbstractPlatformTransactionManager platformTransactionManager, EventSupport eventSupport) {
        this.platformTransactionManager = platformTransactionManager;
        this.eventSupport = eventSupport;
    }


    /**
     * Create a TransactionStatus instance for the given arguments.
     * @param definition transaction definition
     * @param transaction transaction
     * @param newTransaction whether transaction is new
     * @param newSynchronization whether newSynchronization
     * @param debug whether debug
     * @param suspendedResources whether suspend resources
     */
    @Override
    protected DefaultTransactionStatus newTransactionStatus(TransactionDefinition definition, Object transaction, boolean newTransaction, boolean newSynchronization, boolean debug, Object suspendedResources) {
        Object newTransactionStatus = ReflectionUtils.invokeMethod(platformTransactionManager, "newTransactionStatus",
                new Class<?>[]{TransactionDefinition.class, Object.class, boolean.class, boolean.class, boolean.class, Object.class},
                definition, transaction, newTransaction, newSynchronization, debug, suspendedResources);

        return (DefaultTransactionStatus) newTransactionStatus;

    }

    /**
     * Initialize transaction synchronization as appropriate.
     * @param status transaction status
     * @param definition transaction definition
     */
    @Override
    protected void prepareSynchronization(DefaultTransactionStatus status, TransactionDefinition definition) {
        ReflectionUtils.invokeMethod(platformTransactionManager, "prepareSynchronization",
                new Class<?>[]{DefaultTransactionStatus.class, TransactionDefinition.class},
                status, definition);
    }

    /**
     * Determine the actual timeout to use for the given definition.
     * Will fall back to this manager's default timeout if the
     * transaction definition doesn't specify a non-default value.
     * @param definition the transaction definition
     * @return the actual timeout to use
     * @see TransactionDefinition#getTimeout()
     * @see #setDefaultTimeout
     */
    @Override
    protected int determineTimeout(TransactionDefinition definition) {
        Object o = ReflectionUtils.invokeMethod(platformTransactionManager, "determineTimeout",
                new Class<?>[]{TransactionDefinition.class},
                definition);

        return (int) o;
    }

    /**
     * Return a transaction object for the current transaction state.
     * <p>The returned object will usually be specific to the concrete transaction
     * manager implementation, carrying corresponding transaction state in a
     * modifiable fashion. This object will be passed into the other template
     * methods (e.g. doBegin and doCommit), either directly or as part of a
     * DefaultTransactionStatus instance.
     * <p>The returned object should contain information about any existing
     * transaction, that is, a transaction that has already started before the
     * current {@code getTransaction} call on the transaction manager.
     * Consequently, a {@code doGetTransaction} implementation will usually
     * look for an existing transaction and store corresponding state in the
     * returned transaction object.
     * @return the current transaction object
     * @throws CannotCreateTransactionException
     * if transaction support is not available
     * @throws TransactionException in case of lookup or system errors
     * @see #doBegin
     * @see #doCommit
     * @see #doRollback
     * @see DefaultTransactionStatus#getTransaction
     */
    @Override
    protected Object doGetTransaction() throws TransactionException {

        return ReflectionUtils.invokeMethod(platformTransactionManager, "doGetTransaction",
                new Class<?>[]{});
    }

    /**
     * Check if the given transaction object indicates an existing transaction
     * (that is, a transaction which has already started).
     * <p>The result will be evaluated according to the specified propagation
     * behavior for the new transaction. An existing transaction might get
     * suspended (in case of PROPAGATION_REQUIRES_NEW), or the new transaction
     * might participate in the existing one (in case of PROPAGATION_REQUIRED).
     * <p>The default implementation returns {@code false}, assuming that
     * participating in existing transactions is generally not supported.
     * Subclasses are of course encouraged to provide such support.
     * @param transaction transaction object returned by doGetTransaction
     * @return if there is an existing transaction
     * @throws TransactionException in case of system errors
     * @see #doGetTransaction
     */
    @Override
    protected boolean isExistingTransaction(Object transaction) throws TransactionException {
        Object o = ReflectionUtils.invokeMethod(platformTransactionManager, "isExistingTransaction",
                new Class<?>[]{Object.class},
                transaction);

        return (boolean) o;
    }

    /**
     * Return whether to use a savepoint for a nested transaction.
     * <p>Default is {@code true}, which causes delegation to DefaultTransactionStatus
     * for creating and holding a savepoint. If the transaction object does not implement
     * the SavepointManager interface, a NestedTransactionNotSupportedException will be
     * thrown. Else, the SavepointManager will be asked to create a new savepoint to
     * demarcate the startAll of the nested transaction.
     * <p>Subclasses can override this to return {@code false}, causing a further
     * call to {@code doBegin} - within the context of an already existing transaction.
     * The {@code doBegin} implementation needs to handle this accordingly in such
     * a scenario. This is appropriate for JTA, for example.
     * @see DefaultTransactionStatus#createAndHoldSavepoint
     * @see DefaultTransactionStatus#rollbackToHeldSavepoint
     * @see DefaultTransactionStatus#releaseHeldSavepoint
     * @see #doBegin
     */
    @Override
    protected boolean useSavepointForNestedTransaction() {
        Object o = ReflectionUtils.invokeMethod(platformTransactionManager, "useSavepointForNestedTransaction",
                new Class<?>[]{});

        return (boolean) o;
    }

    /**
     * Begin a new transaction with semantics according to the given transaction
     * definition. Does not have to care about applying the propagation behavior,
     * as this has already been handled by this abstract manager.
     * <p>This method gets called when the transaction manager has decided to actually
     * startAll a new transaction. Either there wasn't any transaction before, or the
     * previous transaction has been suspended.
     * <p>A special scenario is a nested transaction without savepoint: If
     * {@code useSavepointForNestedTransaction()} returns "false", this method
     * will be called to startAll a nested transaction when necessary. In such a context,
     * there will be an active transaction: The implementation of this method has
     * to detect this and startAll an appropriate nested transaction.
     * @param transaction transaction object returned by {@code doGetTransaction}
     * @param definition TransactionDefinition instance, describing propagation
     * behavior, isolation level, read-only flag, timeout, and transaction name
     * @throws TransactionException in case of creation or system errors
     */
    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        ReflectionUtils.invokeMethod(platformTransactionManager, "doBegin",
                new Class<?>[]{Object.class, TransactionDefinition.class},
                transaction, definition);
    }

    /**
     * Suspend the resources of the current transaction.
     * Transaction synchronization will already have been suspended.
     * <p>The default implementation throws a TransactionSuspensionNotSupportedException,
     * assuming that transaction suspension is generally not supported.
     * @param transaction transaction object returned by {@code doGetTransaction}
     * @return an object that holds suspended resources
     * (will be kept unexamined for passing it into doResume)
     * @throws TransactionSuspensionNotSupportedException
     * if suspending is not supported by the transaction manager implementation
     * @throws TransactionException in case of system errors
     * @see #doResume
     */
    @Override
    protected Object doSuspend(Object transaction) throws TransactionException {

        return ReflectionUtils.invokeMethod(platformTransactionManager, "doSuspend",
                new Class<?>[]{Object.class},
                transaction);
    }

    /**
     * Resume the resources of the current transaction.
     * Transaction synchronization will be resumed afterwards.
     * <p>The default implementation throws a TransactionSuspensionNotSupportedException,
     * assuming that transaction suspension is generally not supported.
     * @param transaction transaction object returned by {@code doGetTransaction}
     * @param suspendedResources the object that holds suspended resources,
     * as returned by doSuspend
     * @throws TransactionSuspensionNotSupportedException
     * if resuming is not supported by the transaction manager implementation
     * @throws TransactionException in case of system errors
     * @see #doSuspend
     */
    @Override
    protected void doResume(Object transaction, Object suspendedResources) throws TransactionException {
        ReflectionUtils.invokeMethod(platformTransactionManager, "doResume",
                new Class<?>[]{Object.class, Object.class},
                transaction, suspendedResources);
    }

    /**
     * Return whether to call {@code doCommit} on a transaction that has been
     * marked as rollback-only in a global fashion.
     * <p>Does not apply if an application locally sets the transaction to rollback-only
     * via the TransactionStatus, but only to the transaction itself being marked as
     * rollback-only by the transaction coordinator.
     * <p>Default is "false": Local transaction strategies usually don't hold the rollback-only
     * marker in the transaction itself, therefore they can't handle rollback-only transactions
     * as part of transaction commit. Hence, AbstractPlatformTransactionManager will trigger
     * a rollback in that case, throwing an UnexpectedRollbackException afterwards.
     * <p>Override this to return "true" if the concrete transaction manager expects a
     * {@code doCommit} call even for a rollback-only transaction, allowing for
     * special handling there. This will, for example, be the case for JTA, where
     * {@code UserTransaction.commit} will check the read-only flag itself and
     * throw a corresponding RollbackException, which might include the specific reason
     * (such as a transaction timeout).
     * <p>If this method returns "true" but the {@code doCommit} implementation does not
     * throw an exception, this transaction manager will throw an UnexpectedRollbackException
     * itself. This should not be the typical case; it is mainly checked to cover misbehaving
     * JTA providers that silently roll back even when the rollback has not been requested
     * by the calling code.
     * @see #doCommit
     * @see DefaultTransactionStatus#isGlobalRollbackOnly()
     * @see DefaultTransactionStatus#isLocalRollbackOnly()
     * @see TransactionStatus#setRollbackOnly()
     * @see UnexpectedRollbackException
     * @see UserTransaction#commit()
     * @see RollbackException
     */
    @Override
    protected boolean shouldCommitOnGlobalRollbackOnly() {
        Object o = ReflectionUtils.invokeMethod(platformTransactionManager, "shouldCommitOnGlobalRollbackOnly",
                new Class<?>[]{});

        return (boolean) o;
    }

    /**
     * Make preparations for commit, to be performed before the
     * {@code beforeCommit} synchronization callbacks occur.
     * <p>Note that exceptions will get propagated to the commit caller
     * and cause a rollback of the transaction.
     * @param status the status representation of the transaction
     * @throws RuntimeException in case of errors; will be <b>propagated to the caller</b>
     * (note: do not throw TransactionException subclasses here!)
     */
    @Override
    protected void prepareForCommit(DefaultTransactionStatus status) {
        ReflectionUtils.invokeMethod(platformTransactionManager, "prepareForCommit",
                new Class<?>[]{DefaultTransactionStatus.class},
                status);
    }

    /**
     * Perform an actual commit of the given transaction.
     * <p>An implementation does not need to check the "new transaction" flag
     * or the rollback-only flag; this will already have been handled before.
     * Usually, a straight commit will be performed on the transaction object
     * contained in the passed-in status.
     * @param status the status representation of the transaction
     * @throws TransactionException in case of commit or system errors
     * @see DefaultTransactionStatus#getTransaction
     */
    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        ReflectionUtils.invokeMethod(platformTransactionManager, "doCommit",
                new Class<?>[]{DefaultTransactionStatus.class},
                status);
        LOGGER.debug("finished committing transaction");

        LOGGER.debug("begin to handle event");
        eventSupport.handleAndClearEvent();
        LOGGER.debug("finished handling event");
    }

    /**
     * Perform an actual rollback of the given transaction.
     * <p>An implementation does not need to check the "new transaction" flag;
     * this will already have been handled before. Usually, a straight rollback
     * will be performed on the transaction object contained in the passed-in status.
     * @param status the status representation of the transaction
     * @throws TransactionException in case of system errors
     * @see DefaultTransactionStatus#getTransaction
     */
    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        ReflectionUtils.invokeMethod(platformTransactionManager, "doRollback",
                new Class<?>[]{DefaultTransactionStatus.class},
                status);
        LOGGER.debug("finished rollback transaction");

        LOGGER.debug("begin to clear event");
        eventSupport.clearEvent();
        LOGGER.debug("finished clear event");
    }

    /**
     * Set the given transaction rollback-only. Only called on rollback
     * if the current transaction participates in an existing one.
     * <p>The default implementation throws an IllegalTransactionStateException,
     * assuming that participating in existing transactions is generally not
     * supported. Subclasses are of course encouraged to provide such support.
     * @param status the status representation of the transaction
     * @throws TransactionException in case of system errors
     */
    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
        ReflectionUtils.invokeMethod(platformTransactionManager, "doSetRollbackOnly",
                new Class<?>[]{DefaultTransactionStatus.class},
                status);
    }

    /**
     * Register the given list of transaction synchronizations with the existing transaction.
     * <p>Invoked when the control of the Spring transaction manager and thus all Spring
     * transaction synchronizations end, without the transaction being completed yet. This
     * is for example the case when participating in an existing JTA or EJB CMT transaction.
     * <p>The default implementation simply invokes the {@code afterCompletion} methods
     * immediately, passing in "STATUS_UNKNOWN". This is the best we can do if there's no
     * chance to determine the actual outcome of the outer transaction.
     * @param transaction transaction object returned by {@code doGetTransaction}
     * @param synchronizations List of TransactionSynchronization objects
     * @throws TransactionException in case of system errors
     * @see #invokeAfterCompletion(List, int)
     * @see TransactionSynchronization#afterCompletion(int)
     * @see TransactionSynchronization#STATUS_UNKNOWN
     */
    @Override
    protected void registerAfterCompletionWithExistingTransaction(Object transaction, List<TransactionSynchronization> synchronizations) throws TransactionException {
        ReflectionUtils.invokeMethod(platformTransactionManager, "registerAfterCompletionWithExistingTransaction",
                new Class<?>[]{Object.class, List.class},
                transaction, synchronizations);
    }

    /**
     * Cleanup resources after transaction completion.
     * <p>Called after {@code doCommit} and {@code doRollback} execution,
     * on any outcome. The default implementation does nothing.
     * <p>Should not throw any exceptions but just issue warnings on errors.
     * @param transaction transaction object returned by {@code doGetTransaction}
     */
    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        ReflectionUtils.invokeMethod(platformTransactionManager, "doCleanupAfterCompletion",
                new Class<?>[]{Object.class},
                transaction);
    }

}
