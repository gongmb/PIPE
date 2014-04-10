package pipe.actions.type;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import pipe.actions.gui.create.ArcAction;
import pipe.controllers.PetriNetController;
import pipe.controllers.PipeApplicationController;
import pipe.controllers.arcCreator.ArcActionCreator;
import pipe.gui.PetriNetTab;
import pipe.historyActions.AddPetriNetObject;
import pipe.models.component.arc.Arc;
import pipe.models.component.arc.ArcPoint;
import pipe.models.component.place.Place;
import pipe.models.component.token.Token;
import pipe.models.component.transition.Transition;
import pipe.models.petrinet.PetriNet;
import pipe.utilities.transformers.Contains;
import pipe.views.PipeApplicationView;
import pipe.views.arc.InhibitorArcHead;
import pipe.views.arc.TemporaryArcView;
import pipe.visitor.connectable.arc.ArcSourceVisitor;

import javax.swing.event.UndoableEditListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ArcActionTest {
    private ArcAction action;

    @Mock
    private PipeApplicationController mockApplicationController;

    @Mock
    private PetriNetController mockController;

    @Mock
    private PetriNet mockNet;

    @Mock
    private PipeApplicationView mockApplicationView;

    @Mock
    private PetriNetTab mockTab;

    @Mock
    private Token activeToken;

    @Mock
    private ArcSourceVisitor mockSourceVisitor;

    @Mock
    private ArcActionCreator mockCreatorVisitor;

    @Mock
    UndoableEditListener listener;

    @Before
    public void setUp() {
        when(mockController.getPetriNet()).thenReturn(mockNet);

        when(mockApplicationView.getSelectedTokenName()).thenReturn("Default");

        when(mockController.getPetriNetTab()).thenReturn(mockTab);
        when(mockController.getSelectedToken()).thenReturn(activeToken);

        when(mockApplicationController.getActivePetriNetController()).thenReturn(mockController);

        action = new ArcAction("Inhibitor Arc", "Add an inhibitor arc", KeyEvent.VK_H, InputEvent.ALT_DOWN_MASK,
                mockSourceVisitor, mockCreatorVisitor, mockApplicationController, new InhibitorArcHead());
        action.addUndoableEditListener(listener);
    }

    @Test
    public void createsTemporaryArcViewOnClick() {
        Transition transition = new Transition("", "");
        when(mockSourceVisitor.canStart(transition)).thenReturn(true);
        action.doConnectableAction(transition, mockController);

        verify(mockTab).add(any(TemporaryArcView.class));
    }

    @Test
    public void removesTemporaryArcViewOnRealCreation() {
        Transition transition = new Transition("", "");
        when(mockSourceVisitor.canStart(transition)).thenReturn(true);
        action.doConnectableAction(transition, mockController);


        Place place = new Place("", "");
        when(mockCreatorVisitor.canCreate(transition, place)).thenReturn(true);

        action.doConnectableAction(place, mockController);
        verify(mockTab).remove(any(TemporaryArcView.class));
    }

    @Test
    public void callsCreateOnRealCreation() {
        Transition transition = new Transition("", "");
        when(mockSourceVisitor.canStart(transition)).thenReturn(true);
        action.doConnectableAction(transition, mockController);


        Place place = new Place("", "");
        when(mockCreatorVisitor.canCreate(transition, place)).thenReturn(true);

        action.doConnectableAction(place, mockController);
        verify(mockCreatorVisitor).create(eq(transition), eq(place), anyListOf(ArcPoint.class));
    }

    @Test
    public void updatesEndPointOnAction() {
        //TODO: WORK OUT HOW TO TEST SINCE TEMP ARC VIEW ISNT PUBLIC
    }

    @Test
    public void createsUndoAction() {
        Transition transition = new Transition("", "");
        when(mockSourceVisitor.canStart(transition)).thenReturn(true);
        action.doConnectableAction(transition, mockController);

        Place place = new Place("", "");
        when(mockCreatorVisitor.canCreate(transition, place)).thenReturn(true);

        Arc<Transition, Place> mockArc = mock(Arc.class);
        when(mockCreatorVisitor.create(any(Transition.class), any(Place.class), anyListOf(ArcPoint.class))).thenReturn(mockArc);


        action.doConnectableAction(place, mockController);


        AddPetriNetObject addAction = new AddPetriNetObject(mockArc, mockNet);
        verify(listener).undoableEditHappened(argThat(Contains.thisAction(addAction)));
    }

}
