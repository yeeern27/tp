package parser;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

import event.Event;
import event.EventManager;
import ui.UI;
import exception.SyncException;
import command.Command;
import command.DeleteCommand;
import command.DuplicateCommand;
import command.ByeCommand;
import command.AddEventCommand;
import command.EditEventCommand;
import command.ListCommand;

public class Parser {
    private final EventManager eventManager;
    private final UI ui;
    private final Scanner scanner;

    public Parser(EventManager eventManager, UI ui) {
        this.eventManager = eventManager;
        this.ui = ui;
        this.scanner = new Scanner(System.in);
    }

    public Parser(EventManager eventManager, UI ui, Scanner scanner) {
        this.eventManager = eventManager;
        this.ui = ui;
        this.scanner = scanner;
    }

    public Command parse(String input) throws SyncException {
        switch (input.toLowerCase()) {
        case "bye":
            return new ByeCommand();
        case "list":
            return new ListCommand();
        case "add":
            return createAddEventCommand();
        case "delete":
            return createDeleteCommand();
        case "duplicate":
            return createDuplicateCommand();
        case "edit":
            return createEditCommand();
        default:
            throw new SyncException(SyncException.invalidCommandErrorMessage(input));
        }
    }

    private void find(String input) throws SyncException {
        String keyword = input.substring(5).trim().toLowerCase();
        if (keyword.isEmpty()) {
            throw new SyncException("Keyword empty! Type properly.");
        }

        ArrayList<Event> matchingEvents = new ArrayList<>();
        for (Event event : eventManager.getEvents()) {
            if (event.getDescription().toLowerCase().contains(keyword)) {
                matchingEvents.add(event);
            }
        }
        ui.printMatchingEvents(matchingEvents);
    }

    private Command createAddEventCommand() throws SyncException {
        String input = readAddEventInput();

        String[] parts = input.split("\\|");

        if (parts.length != 5) {
            throw new SyncException(SyncException.invalidEventDetailsErrorMessage());
        }

        try {
            String name = parts[0].trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

            LocalDateTime startTime = LocalDateTime.parse(parts[1].trim(), formatter);
            LocalDateTime endTime = LocalDateTime.parse(parts[2].trim(), formatter);

            String location = parts[3].trim();
            String description = parts[4].trim();

            Event newEvent = new Event(name, startTime, endTime, location, description);
            return new AddEventCommand(newEvent);
        } catch (Exception e) {
            throw new SyncException(SyncException.invalidEventDetailsErrorMessage());
        }
    }

    private String readAddEventInput() {
        ui.showAddFormat();
        return scanner.nextLine();
    }

    private Command createDeleteCommand() throws SyncException {
        int index = readDeleteEventIndex();
        return new DeleteCommand(index);
    }

    private int readDeleteEventIndex() throws SyncException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter event index to delete: ");
        try {
            int index = Integer.parseInt(scanner.nextLine()) - 1;
            return index;
        } catch (NumberFormatException e) {
            throw new SyncException("Invalid index format. Use a number.");
        }
    }

    private Command createDuplicateCommand() throws SyncException {
        String input = readDuplicateEventInput();
        String[] parts = input.split(" ", 2);

        if (parts.length < 2) {
            throw new SyncException("Invalid duplicate command format. Use: duplicate index New Event Name");
        }

        try {
            int index = Integer.parseInt(parts[0]) - 1;
            if (index >= 0 && index < eventManager.getEvents().size()) {
                Event eventToDuplicate = eventManager.getEvents().get(index);
                String newName = parts[1];
                return new DuplicateCommand(eventToDuplicate, newName);
            } else {
                throw new SyncException("Invalid event index.");
            }
        } catch (NumberFormatException e) {
            throw new SyncException("Invalid index format. Use a number.");
        }
    }

    private String readDuplicateEventInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter duplicate command (format: duplicate index New Event Name): ");
        return scanner.nextLine();
    }

    private Command createEditCommand() throws SyncException {
        int index = readEditEventIndex();
        return new EditEventCommand(index);
    }

    private int readEditEventIndex() throws SyncException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter event index to edit: ");
        try {
            int index = Integer.parseInt(scanner.nextLine()) - 1;
            return index;
        } catch (Exception e) {
            throw new SyncException("Error in editing event: " + e.getMessage());
        }
    }
}
