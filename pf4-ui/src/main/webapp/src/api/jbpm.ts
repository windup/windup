export interface ApplicationJBPMsDto {
  applicationId: string;
  jbpms: JBPMDto[];
}

export interface JBPMDto {
  fileId?: string;
  fileName: string;
  processName: string;
  processNoteCount: number;
  processDecisionCount: number;
  processStateCount: number;
  processTaskCount: number;
  processSubProcessCount: number;
  actionHandlers: ActionHandlerDto[];
  decisionHandlers: DecisionHandlerDto[];
}

export interface ActionHandlerDto {
  fileId?: string;
  fileName: string;
}

export interface DecisionHandlerDto {
  fileId?: string;
  fileName: string;
}
