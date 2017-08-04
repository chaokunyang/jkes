
$("#request_body_search_form").submit(function (event) {
    event.preventDefault();

    var $form = $(this),
        path = $form.find("#path").val(),
        request_body = $form.find("#request_body").val();

    path = window.location.pathname + "/" + path;

    var target_container = $('#search_result_container');

    if(path.indexOf("_search") == -1) {
        $.get(path, function (dada) {
            fillResult(dada);
        }).fail(function (error) {
            fillError(error);
        });
    }else {
        $.ajax({
            url: path,
            type: 'post',
            contentType: "application/json; charset=utf-8",
            success: function (data) {
                fillResult(data);
            },
            error: function (error) {
                fillError(error);
            },
            data: JSON.stringify(request_body)
        });
    }

    var fillResult = function (data) {
        var json = JSON.stringify(data, null, 4);
        var code = $("<code contenteditable='true'/>").append(json);
        var pre = $("<pre/>").append(code);

        target_container.empty();
        target_container.append(
            pre
        );
        $('pre code').each(function(i, block) {
            hljs.highlightBlock(block);
        });
    };

    function fillError(error) {
        var json = JSON.stringify(error.responseJSON, null, 4);
        var code = $("<code contenteditable='true'/>").append(json);
        var pre = $("<pre/>").append(code);

        target_container.empty();
        target_container.append(
            pre
        );
        $('pre code').each(function(i, block) {
            hljs.highlightBlock(block);
        });
    };
});

