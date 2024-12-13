# Mattermost Connector

Axon Ivy’s mattermost connector helps you to accelerate process automation initiatives by integrating Mattermost features into your process application within no time.

This connector:

- supports you with a demo implementation to reduce your integration effort.
- gives you full power to the [Mattermost's APIs](https://api.mattermost.com/).
- allow you to start the Axon Ivy process by hitting the slash command key from the mattermost's channel.
- allow you to send a message to the mattermost's channel from the Axon Ivy workplace.
- notifies users on the channel for new Axon Ivy workflow Tasks.

## Demo

### Demo sample
1. Hit the slash command key on the channel's chat.
   The Axon Ivy process will be triggered and create a new task.
   The task's information will be sent to the channel by a message.

![call-slash-command](images/slash-command.png)

## Setup

### Setup guideline
Mattermost Instance

1. Ref to [Deploy Mattermost](https://docs.mattermost.com/guides/deployment.html).
2. Create Team, User, ...
3.Enable Bot Account Creation and create a bot account for sending notification to the channel Axon Ivy. E.g. 
   axonivy-bot
4.Create a slash command in the Integrations menu.
   ![create-slash-command](images/create-slash-command.png)

Add the following `Variables` to your `variables.yaml`:

- `Variables.mattermost.baseUrl`
- `Variables.mattermost.accessToken`
- `Variables.mattermost.teamName`
- `Variables.mattermost.botName`

and replace the values with your given setup.

```
# == Variables ==
# 
# You can define here your project Variables.
# If you want to define/override a Variable for a specific Environment, 
# add an additional ‘variables.yaml’ file in a subdirectory in the ‘Config’ folder: 
# '<project>/Config/_<environment>/variables.yaml
#
Variables:
#  myVariable: value
  mattermost:
    # The base URL of matter most
    baseUrl: ""
    # Personal access tokens function similarly to session tokens and can be used by integrations to authenticate against the REST API.
    accessToken: ""
    # The team name
    teamName: ""
    # The name of bot that will inform the task on the channel
    botName: ""
    # This variable is used for getting incoming webhook list per page
    incomingWebhookPerPage: 200

```