import { WCTAInteractionRenderer } from './WCTAInteractionRenderer.js';

export class WCTABalloonsRenderer extends WCTAInteractionRenderer {

    // ------------------------------------
    // ---------- Constructor(s) ----------
    // ------------------------------------

    constructor(controller) {
        super(controller);
    }

    // ---------- Dialogue Step Rendering ----------

    /**
     * Render a step in the dialogue given the information provided in the given dialogueStep object, or
     * render a "Dialogue Finished" statement if the given dialogueStep is null.
     *
     * @param {DialogueStep} dialogueStep the {@link DialogueStep} object to render, or null
     * @param {String} nullMessage the message explaining why dialogueStep is null.
     */
    renderDialogueStep(dialogueStep, nullMessage) {


        var contentBlock = document.getElementById("interaction-tester-content-balloons");
        
        // Create the container element for the Statement
        const statementContainer = document.createElement("div");
        statementContainer.classList.add("dialogue-step-statement-container");
        contentBlock.appendChild(statementContainer);

        if(dialogueStep == null) {
            
            // Create and show a message why the dialogue has ended
            var dialogueOverElement = document.createElement("div");
            dialogueOverElement.classList.add("dialogue-finished-statement");
            if(nullMessage != null) {
                dialogueOverElement.innerHTML = nullMessage;
            } else {
                dialogueOverElement.innerHTML = "Dialogue Finished";
            }
            statementContainer.appendChild(dialogueOverElement);

            // Disable the "cancel dialogue" button
            var cancelButton = document.getElementById("button-cancel-dialogue");
            cancelButton.classList.add("button-disabled");
            cancelButton.setAttribute('title',"You can cancel a dialogue when there is a dialogue in progress.");
            cancelButton.replaceWith(cancelButton.cloneNode(true));
            
            // Create a filler element that fills the "rest" of the scrollable area, to allow a proper
            // scrolling to the top (this element will be removed when rendering the next dialogue step)
            const fillerElement = document.createElement("div");
            fillerElement.setAttribute("id","temp-dialogue-filler");
            fillerElement.classList.add("dialogue-step-filler-element");
            contentBlock.appendChild(fillerElement);

            var contentBlockHeight = contentBlock.getBoundingClientRect().height;
            var statementContainerHeight = statementContainer.getBoundingClientRect().height;
            
            // Set the calculated height of the temporary filler element
            fillerElement.style.height = ((contentBlockHeight - statementContainerHeight) + "px");
            
            // Scroll to the top of scrollable element
            contentBlock.scrollTop = fillerElement.offsetTop;
        } else {

            contentBlock.innerHTML = dialogueStep.speaker + ":" + dialogueStep.statement.fullStatement();


        }

    }

}