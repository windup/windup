import React, { useMemo } from "react";

import { Button, ButtonVariant, Modal } from "@patternfly/react-core";
import { TableComposable, Tbody, Td, Tr } from "@patternfly/react-table";


import { ApplicationDto } from "@app/api/application";
import { useApplicationsDetailsQuery } from "@app/queries/applications-details";
import { RuleEditor } from "@app/shared/components";
import { useModal } from "@app/shared/hooks";

export interface IMessagesCardProps {
  application: ApplicationDto;
}

export const MessagesCard: React.FC<IMessagesCardProps> = ({ application }) => {
  const applicationsDetailsQuery = useApplicationsDetailsQuery();
  const applicationDetails = useMemo(() => {
    return applicationsDetailsQuery.data?.find(
      (f) => f.applicationId === application?.id
    );
  }, [applicationsDetailsQuery.data, application]);

  const ruleModal = useModal<"showRule", string>();

  return (
    <>
      <TableComposable>
        <Tbody>
          {applicationDetails?.messages.map((message, index) => (
            <Tr key={index}>
              <Td>
                {message.value}{" "}
                <Button
                  variant={ButtonVariant.link}
                  onClick={() => ruleModal.open("showRule", message.ruleId)}
                >
                  View rule
                </Button>
              </Td>
            </Tr>
          ))}
        </Tbody>
      </TableComposable>

      <Modal
        title={`Rule: ${ruleModal?.data}`}
        isOpen={ruleModal.isOpen && ruleModal.action === "showRule"}
        onClose={ruleModal.close}
        variant="large"
      >
        {ruleModal.data && <RuleEditor ruleId={ruleModal.data} />}
      </Modal>
    </>
  );
};
