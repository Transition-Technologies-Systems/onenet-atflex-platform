package pl.com.tt.flex.server.domain.schedulingUnit.enumeration;

/**
 * The SchedulingUnitProposalType enumeration.
 * BSP User is owner of SchedulingUnit and he can only invites other User's Units (BSP does not have its own Units).
 * Other Users can send a request to attach the selected Unit to the BSP's SchedulingUnit.
 */
public enum SchedulingUnitProposalType {
    INVITATION, // invitation sent to FSP
    REQUEST; // proposal request sent to BSP
}
