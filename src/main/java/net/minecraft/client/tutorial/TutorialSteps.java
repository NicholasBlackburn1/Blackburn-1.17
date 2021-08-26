package net.minecraft.client.tutorial;

import java.util.function.Function;

public enum TutorialSteps
{
    MOVEMENT("movement", MovementTutorialStepInstance::new),
    FIND_TREE("find_tree", FindTreeTutorialStepInstance::new),
    PUNCH_TREE("punch_tree", PunchTreeTutorialStepInstance::new),
    OPEN_INVENTORY("open_inventory", OpenInventoryTutorialStep::new),
    CRAFT_PLANKS("craft_planks", CraftPlanksTutorialStep::new),
    NONE("none", CompletedTutorialStepInstance::new);

    private final String name;
    private final Function < Tutorial, ? extends TutorialStepInstance > constructor;

    private <T extends TutorialStepInstance> TutorialSteps(String p_120637_, Function<Tutorial, T> p_120638_)
    {
        this.name = p_120637_;
        this.constructor = p_120638_;
    }

    public TutorialStepInstance create(Tutorial pTutorial)
    {
        return this.constructor.apply(pTutorial);
    }

    public String getName()
    {
        return this.name;
    }

    public static TutorialSteps getByName(String pName)
    {
        for (TutorialSteps tutorialsteps : values())
        {
            if (tutorialsteps.name.equals(pName))
            {
                return tutorialsteps;
            }
        }

        return NONE;
    }
}
