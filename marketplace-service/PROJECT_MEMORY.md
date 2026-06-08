# Project Memory

## Project
- Name: Axon Ivy marketplace website backend
- Stack: Spring Boot, Java 21
- Build: Maven multi-module

## Structure
- `core`: shared code for both apps
- `app`: main production app for `market.axonivy.com`
- `stable`: dedicated app for new experimental feature

## Module Roles
- `core` holds common domain, repository, service, and utility code used by both apps.
- `app` is the main marketplace backend for public website traffic and primary features.
- `stable` is separate runtime for experimental or stable-targeted feature work.

## Design Rule
- Project structure is split by function, not by technical layer alone.
- Shared logic belongs in `core` first.
- App-specific logic stays in `app` or `stable` to keep each module focused.
