package org.jboss.windup.config.operation;

import java.util.List;

import org.ocpsoft.rewrite.config.CompositeOperation;
import org.ocpsoft.rewrite.config.Operation;

/**
 * Contains useful functions for operating on Rewrite {@link Operation}s.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class OperationUtil {
    /**
     * Indicates whether or not the provided {@link Operation} is or contains any {@link Operation}s of the specified type. This will recursively
     * check all of the suboperations on {@link CompositeOperation}s as well.
     */
    public static boolean hasOperationType(Operation operation, Class<? extends Operation> operationType) {
        if (operation == null)
            return false;

        if (operationType.isAssignableFrom(operation.getClass()))
            return true;

        if (operation instanceof CompositeOperation) {
            List<Operation> operations = ((CompositeOperation) operation).getOperations();
            for (Operation childOperation : operations) {
                if (hasOperationType(childOperation, operationType))
                    return true;
            }
        }

        return false;
    }

    /**
     * Indicates whether the operation contains any {@link Commit} operations.
     */
    public static boolean hasCommitOperation(Operation operation) {
        return hasOperationType(operation, Commit.class);
    }

    /**
     * Indicates whether the operation contains any {@link IterationProgress} operations.
     */
    public static boolean hasIterationProgress(Operation operation) {
        return hasOperationType(operation, IterationProgress.class);
    }

}
