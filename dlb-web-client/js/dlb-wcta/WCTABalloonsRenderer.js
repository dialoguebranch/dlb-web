import { WCTAInteractionRenderer } from './WCTAInteractionRenderer.js';

export class WCTABalloonsRenderer extends WCTAInteractionRenderer {

    // ------------------------------------
    // ---------- Constructor(s) ----------
    // ------------------------------------

    constructor(controller) {
        super(controller);
    }

    get contentBlock() {
        return this._contentBlock;
    }

    get avatarBlock() {
        return this._avatarBlock;
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

        console.log("Balloons Renderer asked to render the following dialogueStep: ");
        console.log(dialogueStep);

        if(this._contentBlock == null) {
            this._contentBlock = document.getElementById("interaction-tester-content-balloons");
        }
        
        // Create the container element for the Statement
        const statementContainer = document.createElement("div");
        statementContainer.classList.add("dialogue-step-statement-container");
        this.contentBlock.appendChild(statementContainer);

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
            
            
        } else {

            // Render the correct Agent Avatar
            this.renderAgentAvatar(dialogueStep.speaker);

            // Create a statement balloon
            const statementBalloon = document.createElement("div");
            statementBalloon.classList.add("int-balloon-statement-balloon");
            statementBalloon.innerHTML = dialogueStep.statement.fullStatement();

                // Calculate and set the width of the balloon element
                //var contentBlockWidth = this.contentBlock.getBoundingClientRect().width;
                //var avatarBlockWidth = this.avatarBlock.getBoundingClientRect().width;
                //var statementBalloonWidth = contentBlockWidth - avatarBlockWidth;
                //statementBalloon.style.width = statementBalloonWidth + "px";

            // Add the statementBalloon to the main container
            this.contentBlock.appendChild(statementBalloon);

            //this.contentBlock.innerHTML = dialogueStep.speaker + ":" + dialogueStep.statement.fullStatement();

        }

    }

    renderAgentAvatar(speaker) {
        if(this.avatarBlock == null) {
            this._avatarBlock = document.createElement("div");
            this._avatarBlock.classList.add("int-balloon-avatar");
            this.contentBlock.appendChild(this._avatarBlock);
        }
        
        if(speaker == "Martin McOwl") {
            this.avatarBlock.style.backgroundImage="url(img/avatar-martin.png)";
        } else {
            // Todo: get a random avatar for any new speaker name we encounter (and then store it)
            this.avatarBlock.style.backgroundImage="url(img/avatar-green.png)";
        }

        this.avatarBlock.title = speaker;
    }

}