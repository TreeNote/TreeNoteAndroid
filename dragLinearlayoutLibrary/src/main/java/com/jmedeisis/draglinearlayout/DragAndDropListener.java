package com.jmedeisis.draglinearlayout;

import android.support.annotation.Nullable;
import android.view.View;

public interface DragAndDropListener {

    /**
     * Wird aufgerufen, bevor angefangen wird mit dem DragHandler im DragLinearLayout ein Drag zu starten
     *
     * @param draggedView, zu der der DragHandler gehört
     */
    void onDragStart(View draggedView);

    /**
     * Wird ausgeführt, nachdem der DragHandler im DragLinearLayout gedropped wurde
     *
     * @param droppedView,  zu der der DragHandler gehört
     * @param droppedOnView View, auf die gedropped wurde
     * @param aboveView     View, die sich über der droppedView befindet
     * @param belowView     View, die sich unter der droppedView befindet
     */
    void onDrop(View droppedView, @Nullable View droppedOnView, View aboveView, View belowView);

    /**
     * @param draggedView     View, die gedraggt wird
     * @param draggedOverView View, über die gedraggt wird
     */
    void onDragOverEnter(View draggedView, View draggedOverView);

    /**
     * Diese Methode wird aufgerufen, nachdem die draggedView die formerDraggedOverView verlassen hat.
     * Diese Methode wird NICHT! aufgerufen, wenn die draggedView auf die droppedView gedropped wird!
     *
     * @param draggedView           View, die gedraggt wird
     * @param formerDraggedOverView View, über die gedraggt wurde
     * @param newAboveView          View, die sich über der draggedView befindet
     * @param newBelowView          View, die sich unter der draggedView befindet
     */
    void onDragOverLeave(View draggedView, View formerDraggedOverView, View newAboveView, View newBelowView);
}
