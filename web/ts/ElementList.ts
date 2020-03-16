
/**
 * The ElementList Singleton provides a way of displaying all of the data 
 * stored on the server as an HTML table.
 */
class ElementList {
    /**
     * The name of the DOM entry associated with ElementList
     */
    private static readonly NAME = "ElementList";

    /**
     * Track if the Singleton has been initialized
     */
    private static isInit = false;

    /**
     * Initialize the ElementList singleton.  
     * This needs to be called from any public static method, to ensure that the 
     * Singleton is initialized before use.
     */
    private static init() {
        if (!ElementList.isInit) {
            ElementList.isInit = true;
        }
    }

    /**
     * refresh() is the public method for updating the ElementList
     */
    public static refresh() {
        // Make sure the singleton is initialized
        ElementList.init();
        // Issue a GET, and then pass the result to update()
        $.ajax({
            type: "GET",
            url: "/messages",
            dataType: "json",
            success: ElementList.update
        });
    }

    /**
     * update() is the private method used by refresh() to update the 
     * ElementList
     */
    private static update(data: any) {
        // Remove the table of data, if it exists
        $("#" + ElementList.NAME).remove();
        // Use a template to re-generate the table, and then insert it
        $("body").append(Handlebars.templates[ElementList.NAME + ".hb"](data));
        // Find all of the Upvote buttons, and set their behavior
        $("." + ElementList.NAME + "-upvotebtn").click(ElementList.clickUpVote);
        // Find all of the Upvote buttons, and set their behavior
        $("." + ElementList.NAME + "-downvotebtn").click(ElementList.clickDownVote);
    }


    /**
     * buttons() creates 'upvote' and 'downvote' buttons for an id, and puts them in
     * a TD
     */
    private static buttons(id: string): string {
        return "<td><button class='" + ElementList.NAME +
            "-upvotebtn' data-value='" + id + "'>Like</button></td>" +
            "<td><button class='" + ElementList.NAME +
            "-downvotebtn' data-value='" + id + "'>Dislike</button></td>";
    }

    /**
     * clickUpvote is the code we run in response to a click of a upvote button
    */

    private static clickUpVote() {

        // as in clickDelete, we need the ID of the row
        let id = $(this).data("value");
        $.ajax({
            type: "POST",
            url: "/upvote/",
            dataType: "json",
            data: JSON.stringify({ mId: id }),
            success: ElementList.refresh
        });
    }

    /**
     * clickDownVote is the code we run in response to a click of a downvote button
    */

    private static clickDownVote() {

    // as in clickDelete, we need the ID of the row
    let id = $(this).data("value");
    $.ajax({
        type: "POST",
        url: "/downvote/",
        dataType: "json",
        data: JSON.stringify({ mId: id }),
        success: ElementList.refresh
    });
}
    

}
